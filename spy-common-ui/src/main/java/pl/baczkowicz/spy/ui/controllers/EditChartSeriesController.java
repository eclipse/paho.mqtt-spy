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
package pl.baczkowicz.spy.ui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.charts.ChartSeriesTypeEnum;
import pl.baczkowicz.spy.ui.events.SaveChartSeriesEvent;
import pl.baczkowicz.spy.ui.properties.ChartSeriesProperties;

/**
 * Controller for editing chart series.
 */
@SuppressWarnings({})
public class EditChartSeriesController extends AnchorPane implements Initializable
{
	// UI & Formatting
				
	@FXML
	private TextField topicText;
	
	@FXML
	private TextField expressionText;
	
	@FXML
	private ComboBox<ChartSeriesTypeEnum> typeCombo;
	
	@FXML
	private TextField nameText;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button saveButton;
	
	private IKBus eventBus;

	private ChartSeriesProperties editedProperties;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{		
		typeCombo.setCellFactory(new Callback<ListView<ChartSeriesTypeEnum>, ListCell<ChartSeriesTypeEnum>>()
		{
			@Override
			public ListCell<ChartSeriesTypeEnum> call(ListView<ChartSeriesTypeEnum> l)
			{
				return new ListCell<ChartSeriesTypeEnum>()
				{
					@Override
					protected void updateItem(ChartSeriesTypeEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{	
							setText(item.value());							
						}
					}
				};
			}
		});
		typeCombo.setConverter(new StringConverter<ChartSeriesTypeEnum>()
		{
			@Override
			public String toString(ChartSeriesTypeEnum item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{					
					return item.value();
				}
			}

			@Override
			public ChartSeriesTypeEnum fromString(String id)
			{
				return null;
			}
		});
		
		typeCombo.getItems().addAll(ChartSeriesTypeEnum.values());
	}

	public void init()
	{
		// 
	}
	
	@FXML
	private void cancel()
	{
		final Stage stage = (Stage) nameText.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	private void save()
	{							
		if (editedProperties == null)
		{
			final ChartSeriesProperties updatedProperties = new ChartSeriesProperties(
					0, 
					nameText.getText(), 
					topicText.getText(), 
					typeCombo.getSelectionModel().getSelectedItem(), 
					expressionText.getText());

			eventBus.publish(new SaveChartSeriesEvent(updatedProperties, true));
		}
		else
		{
			editedProperties.nameProperty().set(nameText.getText());
			editedProperties.topicProperty().set(topicText.getText());
			editedProperties.typeProperty().set(typeCombo.getSelectionModel().getSelectedItem());
			editedProperties.valueExpressionProperty().set(expressionText.getText());

			eventBus.publish(new SaveChartSeriesEvent(editedProperties, false));
		}		
		
		final Stage stage = (Stage) nameText.getScene().getWindow();
		stage.close();
	}
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	public void populateValues(final ChartSeriesProperties editedProperties)
	{
		this.editedProperties = editedProperties;
		
		if (editedProperties == null)
		{
			nameText.setText("");
			topicText.setText("");
			typeCombo.getSelectionModel().select(0);
			expressionText.setText("");
		}
		else
		{
			nameText.setText(editedProperties.getName());
			topicText.setText(editedProperties.getTopic());
			typeCombo.getSelectionModel().select(editedProperties.typeProperty().get());
			expressionText.setText(editedProperties.valueExpressionProperty().get());
		}		
	}
}
