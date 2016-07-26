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
package pl.baczkowicz.spy.ui.properties;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.baczkowicz.spy.scripts.ScriptChangeObserver;
import pl.baczkowicz.spy.testcases.TestCaseStatus;
import pl.baczkowicz.spy.testcases.TestCaseStep;

/**
 * This represents a single row displayed in the test case table.
 */
public class TestCaseStepProperties implements ScriptChangeObserver
{
	/** The step number. */
	private SimpleStringProperty stepNumberProperty;
	
	/** Description of the step. */	
	private SimpleStringProperty descriptionProperty;
	
	/** Step status. */	
	private SimpleObjectProperty<TestCaseStatus> statusProperty;
	
	/** Information about the execution. */	
	private SimpleStringProperty executionInfoProperty;
	
	private TestCaseStep step;

	public TestCaseStepProperties(final TestCaseStep step)
	{
		this.step = step;
		
		this.stepNumberProperty = new SimpleStringProperty(step.getStepNumber());
		this.descriptionProperty = new SimpleStringProperty(step.getDescription());
		this.statusProperty = new SimpleObjectProperty<>(step.getStatus());
		this.executionInfoProperty = new SimpleStringProperty(step.getExecutionInfo());
	}

	public void update()
	{
		executionInfoProperty().setValue(step.getExecutionInfo());
		statusProperty().setValue(step.getStatus());
	}

	/**
	 * The description property.
	 * 
	 * @return The description as SimpleStringProperty
	 */
	public SimpleStringProperty descriptionProperty()
	{
		return this.descriptionProperty;
	}
	
	/**
	 * The stepNumber property.
	 * 
	 * @return The stepNumber as SimpleStringProperty
	 */
	public SimpleStringProperty stepNumberProperty()
	{
		return this.stepNumberProperty;
	}
	
	/**
	 * The executionInfo property.
	 * 
	 * @return The executionInfo as SimpleStringProperty
	 */
	public SimpleStringProperty executionInfoProperty()
	{
		return this.executionInfoProperty;
	}
	
	/**
	 * The statusProperty property.
	 * 
	 * @return The statusProperty as SimpleStringProperty
	 */
	public SimpleObjectProperty<TestCaseStatus> statusProperty()
	{
		return this.statusProperty;
	}

	@Override
	public void onChange()
	{
		update();		
	}
}
