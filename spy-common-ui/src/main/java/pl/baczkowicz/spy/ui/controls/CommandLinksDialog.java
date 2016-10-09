/***********************************************************************************
 * 
 * Copyright (c) 2013-2015 ControlsFX, Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD 3-Clause License which 
 * accompany this distribution.
 *    
 * The BSD 3-Clause License is available at
 *    http://opensource.org/licenses/BSD-3-Clause
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contributors:
 * 
 *		ControlsFX - initial implementation
 *		Kamil Baczkowicz - minor changes, derivative work created from ControlsFX (http://fxexperience.com/controlsfx/)
 *    
 */
package pl.baczkowicz.spy.ui.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.ui.utils.ImageUtils;

public class CommandLinksDialog
{
	/** Diagnostic logger. */
	// private final static Logger logger = LoggerFactory.getLogger(CommandLinksDialog.class);
	
	/**
     * Show a dialog filled with provided command links. Command links are used instead of button bar and represent 
     * a set of available 'radio' buttons
	 * @param message 
	 * @param string 
     * @param defaultCommandLink command is set to be default. Null means no default
     * @param links list of command links presented in specified sequence 
     * @return action used to close dialog (it is either one of command links or CANCEL) 
     */
    public static Optional<DialogAction> showCommandLinks(final String title, final String message, 
    		final DialogAction defaultCommandLink, final List<DialogAction> links, 
    		final double windowHeight, final int minWidth, final int longMessageMinHeight, double maxHeight, final List<String> stylesheets) 
    {
        final Dialog<DialogAction> dialog = new Dialog<DialogAction>();
        dialog.setTitle(title);
        dialog.getDialogPane().getScene().getStylesheets().addAll(stylesheets);
        dialog.getDialogPane().getButtonTypes().clear();
        
     	dialog.setGraphic(ImageUtils.createIcon("dialog-information-large", 55));
        dialog.setResizable(true);
        dialog.setHeight(windowHeight);
        
        Label label = new Label(message);
		label.setAlignment(Pos.TOP_LEFT);
		label.setTextAlignment(TextAlignment.LEFT);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.setWrapText(true);
		label.getStyleClass().add("command-link-message");

        final int gapSize = 10;
        final List<Button> buttons = new ArrayList<>(links.size());
        
		GridPane content = new GridPane()
		{
			@Override
			protected double computePrefWidth(double height)
			{
				double pw = 0;

				for (int i = 0; i < buttons.size(); i++)
				{
					Button btn = buttons.get(i);
					pw = Math.min(pw, btn.prefWidth(-1));
				}
				return pw + gapSize;
			}

			@Override
			protected double computePrefHeight(double width)
			{
				double ph = 10;

				for (int i = 0; i < buttons.size(); i++)
				{
					Button btn = buttons.get(i);
					ph += btn.prefHeight(width) + gapSize;
				}
				return ph * 1.5;
			}
		};
        
		int row = 0;
		content.add(label, 0, row++);
		content.setMinWidth(minWidth);
		content.setMaxHeight(windowHeight);
		content.setPrefHeight(windowHeight);
        content.setHgap(gapSize);
        content.setVgap(gapSize);
        
		for (final DialogAction commandLink : links)
		{
			if (commandLink == null)
			{
				continue;
			}

			final Button button = buildCommandLinkButton(commandLink, longMessageMinHeight, maxHeight);
			button.setDefaultButton(commandLink == defaultCommandLink);
			button.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent ae)
				{
					dialog.setResult(commandLink);
				}
			});
			commandLink.setButton(button);

			GridPane.setHgrow(button, Priority.ALWAYS);
			GridPane.setVgrow(button, Priority.ALWAYS);
			content.add(button, 0, row++);
			buttons.add(button);
		}
        
        // last button gets some extra padding (hacky)
        GridPane.setMargin(buttons.get(buttons.size() - 1), new Insets(0,0,10,0));
        
        dialog.getDialogPane().setContent(content);
        ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(cancel);
        
        return dialog.showAndWait();
    }    
    
	private static Button buildCommandLinkButton(DialogAction commandLink, final int longMessageMinHeight, double maxHeight) 
    {
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button");
        button.setMaxHeight(maxHeight);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        final Label titleLabel = new Label(commandLink.getHeading() );
        titleLabel.minWidthProperty().bind(new DoubleBinding() 
        {
            {
                bind(titleLabel.prefWidthProperty());
            }
            
            @Override protected double computeValue() {
                return titleLabel.getPrefWidth() + 400;
            }
        });
        titleLabel.getStyleClass().addAll("line-1");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        GridPane.setVgrow(titleLabel, Priority.NEVER);

        Label messageLabel = new Label(commandLink.getLongText() );
        messageLabel.setMinHeight(longMessageMinHeight);
        messageLabel.setPrefHeight(longMessageMinHeight + 10);
        messageLabel.getStyleClass().addAll("line-2");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(messageLabel, Priority.ALWAYS);
        
        Pane graphicContainer = new Pane(ImageUtils.createIcon("go-next-green", 20));
        graphicContainer.getStyleClass().add("graphic-container");
        GridPane.setValignment(graphicContainer, VPos.TOP);
        GridPane.setMargin(graphicContainer, new Insets(0,15,0,0));
        
        GridPane grid = new GridPane();
        grid.minWidthProperty().bind(titleLabel.prefWidthProperty());
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.getStyleClass().add("container");
        grid.add(graphicContainer, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        button.minWidthProperty().bind(titleLabel.prefWidthProperty());
        
        return button;
    }
}
