/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */

package pl.baczkowicz.spy.ui.controls;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;

public class TextAreaWrapper implements TextAreaInterface
{
	private TextArea dataField;

	public TextAreaWrapper(final TextArea textArea)
	{
		this.dataField = textArea;
	}
	
	@Override
	public void setEditable(boolean editable)
	{
		dataField.setEditable(editable);		
	}

	@Override
	public void setWrapText(boolean wrapText)
	{
		dataField.setWrapText(wrapText);		
	}

	@Override
	public ObservableValue<String> selectedTextProperty()
	{
		return dataField.selectedTextProperty();
	}

	@Override
	public void setTooltip(Tooltip tooltip)
	{
		dataField.setTooltip(tooltip);		
	}

	@Override
	public void clear()
	{
		dataField.clear();		
	}

	@Override
	public void appendText(String text)
	{
		dataField.appendText(text);		
	}

	@Override
	public void positionCaret(int position)
	{
		dataField.positionCaret(position);		
	}

	@Override
	public String getSelectedText()
	{
		return dataField.getSelectedText();
	}
	
	@Override
	public String getText()
	{
		return dataField.getText();
	}
}
