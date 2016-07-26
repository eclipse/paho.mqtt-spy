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

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pl.baczkowicz.spy.scripts.ScriptChangeObserver;
import pl.baczkowicz.spy.testcases.TestCase;
import pl.baczkowicz.spy.testcases.TestCaseStatus;
import pl.baczkowicz.spy.testcases.TestCaseStep;
import pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum;

/**
 * This represents a single row displayed in the test cases table.
 */
public class TestCaseProperties implements ScriptChangeObserver
{
	private SimpleObjectProperty<TestCaseStatus> statusProperty;
	
	// TODO: is that needed?
	private SimpleObjectProperty<ScriptTypeEnum> typeProperty;

	private SimpleStringProperty lastUpdatedProperty;

	private SimpleLongProperty countProperty;
		
	private TestCase testCase;
	
	private ObservableList<TestCaseStepProperties> steps = FXCollections.observableArrayList();
	
	public TestCaseProperties(final TestCase testCase)
	{
		this.testCase = testCase;
		
		this.statusProperty = new SimpleObjectProperty<TestCaseStatus>(TestCaseStatus.NOT_RUN);		
		this.typeProperty = new SimpleObjectProperty<ScriptTypeEnum>(ScriptTypeEnum.PUBLICATION);
		this.lastUpdatedProperty = new SimpleStringProperty("");
		this.countProperty = new SimpleLongProperty(0);
		
		for (final TestCaseStep step : testCase.getSteps())
		{
			final TestCaseStepProperties properties = new TestCaseStepProperties(step);
			step.setObserver(properties);
			steps.add(properties);
		}

		update();
	}
	
	public void update()
	{
		lastUpdatedProperty.setValue(testCase.getLastUpdated());
		statusProperty.setValue(testCase.getTestCaseStatus());
	}
	
	public SimpleObjectProperty<TestCaseStatus> statusProperty()
	{
		return this.statusProperty;
	}
	
	public SimpleObjectProperty<ScriptTypeEnum> typeProperty()
	{
		return this.typeProperty;
	}
	
	public SimpleStringProperty lastUpdatedProperty()
	{
		return this.lastUpdatedProperty;
	}
	
	public SimpleLongProperty countProperty()
	{
		return this.countProperty;
	}
	
	/**
	 * Gets the script name.
	 * 
	 * @return Name of the script
	 */
	public String getName()
	{
		return testCase.getName();
	}
	
	public TestCase getScript()
	{
		return testCase;
	}

	@Override
	public void onChange()
	{
		update();		
	}

	public ObservableList<TestCaseStepProperties> getSteps()
	{
		return steps;
	}
}
