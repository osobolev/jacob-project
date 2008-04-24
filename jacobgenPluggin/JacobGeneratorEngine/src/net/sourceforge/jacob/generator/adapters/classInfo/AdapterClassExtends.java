/*
  Copyright (C) 2007  Robert Searle  
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */

package net.sourceforge.jacob.generator.adapters.classInfo;

import java.util.StringTokenizer;

import net.sourceforge.jacob.generator.adapters.AdapterUtilities;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

public class AdapterClassExtends extends BaseAdapterClass {
	final static private Logger log = Logger.getLogger(AdapterClassExtends.class);

	public AdapterClassExtends(final EPackage companyPackage, final EClass classModel) {
		super("EXTENDS;", companyPackage, classModel);
	}

	@Override
	public void process(final String textLine) {
		final StringTokenizer st = new StringTokenizer(textLine.substring(begining().length()).trim(), ";");
		final String superClass = AdapterUtilities.changeUnderscoredName(st.nextToken().trim());

		log.debug("\t" + superClass);
		EClass classifier = (EClass) companyPackage.getEClassifier(superClass);
		assert (classifier != null);
		if ("IDispatch".equals(superClass)) {
			final EClassifier disClassifier = AdapterUtilities.getClassifier(superClass, companyPackage);
			classifier = disClassifier.eClass();
		}
		assert (classifier != null);
		classModel.getESuperTypes().add(classifier);
	}
}