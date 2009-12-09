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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A color palette. Basically a named list of color values.
 * 
 * @author DanielWeber
 */
public class ColorPalette
{
   private String          name;
   private List<ColorCode> colors = new ArrayList<ColorCode>();

   /**
    * @param c to be added
    */
   public void addColor(ColorCode c)
   {
      colors.add(c);
   }

   /**
    * @param name Of this palette
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return This palette's name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return An unmodifiable list of this palette's colors
    */
   public List<ColorCode> getColors()
   {
      return Collections.unmodifiableList(colors);
   }
}
