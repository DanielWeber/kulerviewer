/*******************************************************************************
 * Copyright (c) 2000, 2009 Daniel Weber and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Weber - initial API and implementation
 *******************************************************************************/
package com.github.danielweber.kulerviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * The viewer's main class
 *
 * @author DanielWeber
 */
public class Main
{
   private static MessageBox msg;

   /**
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      if(args.length == 1)
      {
         try
         {
            displayPalette(parseAseFile(args[0]));
         }
         catch(FileNotFoundException fnf)
         {
            error(fnf.getMessage());
         }
      }
      else
      {
         displayPalette(null);
      }
   }

   /**
    * Displays the given message in a dialog if possible.
    *
    * @param message
    */
   private static void error(String message)
   {
      msg = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
      msg.setText("An error occurred");
      msg.setMessage(message);
      msg.open();
   }

   private static ColorPalette parseAseFile(String pathname)
         throws FileNotFoundException, IOException
   {
      File ase = new File(pathname);
      FileInputStream inStream = new FileInputStream(ase);
      ByteBuffer buf = ByteBuffer.allocate((int)inStream.getChannel().size() * 2);
      buf.order(ByteOrder.BIG_ENDIAN);
      inStream.getChannel().read(buf);

      buf.rewind();

      byte[] signature = new byte[4];
      byte[] expectedSignature = new byte[] { 'A', 'S', 'E', 'F' };
      buf.get(signature);
      if(!Arrays.equals(expectedSignature, signature))
      {
         throw new IOException("'" + ase.getAbsolutePath()
               + "' is not an .ase file. Signature does not match!");
      }
      /* int major = */buf.getShort();
      /* int minor = */buf.getShort();
      int noBlocks = buf.getInt();
      ColorPalette palette = new ColorPalette();
      for(int i = 0; i < noBlocks; ++i)
      {
         readBlock(buf, palette);
      }
      return palette;
   }

   private static void displayPalette(ColorPalette palette)
   {
      Shell s = getShell();
      final Display d = s.getDisplay();
      s.setLayout(new FillLayout());

      if(null != palette)
      {
         s.setText(palette.getName());
         addColorButtons(palette, s);
      }
      else
      {
         s.setText("Drop an .ase file to display color values");
      }

      setDropTarget(s);

      s.setSize(800, 200);
      s.open();
      while(!s.isDisposed())
      {
         if(!d.readAndDispatch())
            d.sleep();
      }
      d.dispose();
   }

   private static Shell shell;

   private static Shell getShell()
   {
      if(null == shell)
      {
         shell = new Shell(Display.getDefault());
      }
      return shell;
   }

   private static void addColorButtons(ColorPalette palette, final Shell s)
   {
      s.setText(palette.getName());
      for(ColorCode code : palette.getColors())
      {
         // Button bt = new Button(s, SWT.PUSH);
         Label bt = new Label(s, 0);
         Color color = new Color(s.getDisplay(), code.getR(), code.getG(), code.getB());
         bt.setBackground(color);
         bt.setToolTipText(MessageFormat.format(
               "{0} / ({1},{2},{3})\nClick left to copy hex string to clipboard", code
                     .getName(), code.getR(), code.getG(), code.getB()));
         bt.setData(ColorCode.class.getName(), code);
         bt.setData(Color.class.getName(), color);
         bt.addDisposeListener(new DisposeListener()
         {
            public void widgetDisposed(DisposeEvent e)
            {
               ((Color)e.widget.getData(Color.class.getName())).dispose();
            }
         });
         bt.addMouseListener(new MouseListener()
         {
            public void mouseUp(MouseEvent e)
            {
               if(e.button == 1)
               {
                  ColorCode color = (ColorCode)e.widget
                        .getData(ColorCode.class.getName());
                  Clipboard c = new Clipboard(s.getDisplay());
                  TextTransfer textTransfer = TextTransfer.getInstance();
                  c.setContents(new Object[] { color.getName() },
                        new Transfer[] { textTransfer });
               }
            }

            public void mouseDown(MouseEvent e)
            {
            }

            public void mouseDoubleClick(MouseEvent e)
            {
            }
         });
      }
   }

   private static void readBlock(ByteBuffer buf, ColorPalette palette)
   {
      buf.mark();
      short blockType = buf.getShort();
      /* int blockLength = */buf.getInt();
      short nameLen = buf.getShort();
      switch(blockType)
      {
         case (short)0xc001:
            // group start
            String name = readString(buf);
            System.out.println("Starting group '" + name + "'");
            palette.setName(name);
            break;
         case (short)0xc002:
            // group end
            buf.position(buf.position() + nameLen);
            break;
         case 0x0001:
            // color entry
            String colorName = readString(buf);
            // 4 byte color model
            byte first = buf.get();
            buf.get();
            buf.get();
            buf.get();
            if('R' == first)
            {
               int r = (int)(buf.getFloat() * 255);
               int g = (int)(buf.getFloat() * 255);
               int b = (int)(buf.getFloat() * 255);
               short type = buf.getShort();
               String types[] = new String[] { "Global", "Spot", "Normal" };
               System.out.printf("   %s (%3d, %3d, %3d) / %s", colorName, r, g, b,
                     types[type]);
               System.out.println();
               palette.addColor(new ColorCode(colorName, r, g, b));
            }
            else if('C' == first)
            {
               throw new RuntimeException("Unable to handle CMYK colors");
            }
            else if('L' == first)
            {
               throw new RuntimeException("Unable to handle LAB colors");
            }
            else if('G' == first)
            {
               throw new RuntimeException("Unable to handle Gray colors");
            }

            break;
      }
   }

   /**
    * Reads a null terminated string from the given buffer.
    * 
    * @param buf to read from
    * @return The string that has been read from the buffer's current position
    */
   private static String readString(ByteBuffer buf)
   {
      final StringBuilder ret = new StringBuilder(10);
      char next = buf.getChar();
      while(next != (char)0)
      {
         ret.append(next);
         next = buf.getChar();
      }
      return ret.toString();
   }

   public static void setDropTarget(final Shell shell)
   {
      int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
      DropTarget target = new DropTarget(shell, operations);
      target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
      target.addDropListener(new DropTargetAdapter()
      {
         public void dragEnter(DropTargetEvent e)
         {
            if(e.detail == DND.DROP_NONE)
               e.detail = DND.DROP_LINK;
         }

         public void dragOperationChanged(DropTargetEvent e)
         {
            if(e.detail == DND.DROP_NONE)
               e.detail = DND.DROP_LINK;
         }

         public void drop(DropTargetEvent event)
         {
            if(event.data == null)
            {
               event.detail = DND.DROP_NONE;
               return;
            }
            if(event.data instanceof String[])
            {
               String[] files = (String[])event.data;
               try
               {
                  cleanShell(shell);
                  addColorButtons(parseAseFile(files[0]), shell);
                  shell.layout(true, true);
               }
               catch(FileNotFoundException e)
               {
                  error(e.getMessage());
               }
               catch(IOException e)
               {
                  error(e.getMessage());
               }
            }
         }

         private void cleanShell(Shell shell)
         {
            for(Control c : shell.getChildren())
            {
               c.dispose();
            }
         }
      });
   }

}
