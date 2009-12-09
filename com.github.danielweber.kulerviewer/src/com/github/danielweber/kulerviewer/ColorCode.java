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

/**
 * 
 * @author DanielWeber
 */
public class ColorCode
{
   private final int    r;
   private final int    g;
   private final int    b;
   private final String name;

   public ColorCode(String name, int r, int g, int b)
   {
      this.name = name;
      this.r = r;
      this.g = g;
      this.b = b;
   }

   public String getName()
   {
      return name;
   }

   public int getR()
   {
      return r;
   }

   public int getG()
   {
      return g;
   }

   public int getB()
   {
      return b;
   }

   public ColorCode invert()
   {
      return new ColorCode("Inverse of " + getName(), invert(r), invert(g), invert(b));
   }

   private int invert(int value)
   {
      return 255 - value;
   }
}
