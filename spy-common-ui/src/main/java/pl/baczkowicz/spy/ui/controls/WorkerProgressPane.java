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
 *		Kamil Baczkowicz - minor changes to work with Java dialogs, derivative work created from ControlsFX (http://fxexperience.com/controlsfx/)
 *    
 */
package pl.baczkowicz.spy.ui.controls;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;

public class WorkerProgressPane extends Region
{
	private Worker<?> worker;

	private boolean dialogVisible = false;
	private boolean cancelDialogShow = false;

	private ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>()
	{
		@Override
		public void changed(ObservableValue<? extends State> observable,
				State old, State value)
		{
			switch (value)
			{
			case CANCELLED:
			case FAILED:
			case SUCCEEDED:
				if (!dialogVisible)
				{
					cancelDialogShow = true;
				}
				else if (old == State.SCHEDULED || old == State.RUNNING)
				{
					end();
				}
				break;
			case SCHEDULED:
				begin();
				break;
			default: // no-op
			}
		}
	};

	public final void setWorker(final Worker<?> newWorker)
	{
		if (newWorker != worker)
		{
			if (worker != null)
			{
				worker.stateProperty().removeListener(stateListener);
				end();
			}

			worker = newWorker;

			if (newWorker != null)
			{
				newWorker.stateProperty().addListener(stateListener);
				if (newWorker.getState() == Worker.State.RUNNING
						|| newWorker.getState() == Worker.State.SCHEDULED)
				{
					// It is already running
					begin();
				}
			}
		}
	}

	// If the progress indicator changes, then we need to re-initialize
	// If the worker changes, we need to re-initialize

	private final Alert dialog;
	private final ProgressBar progressBar;

	public WorkerProgressPane(Alert dialog)
	{
		this.dialog = dialog;

		this.progressBar = new ProgressBar();
		progressBar.setMaxWidth(Double.MAX_VALUE);
		getChildren().add(progressBar);

		if (worker != null)
		{
			progressBar.progressProperty().bind(worker.progressProperty());
		}
	}

	private void begin()
	{
		// Platform.runLater needs to be used to show the dialog because
		// the call begin() is going to be occurring when the worker is
		// notifying state listeners about changes. If Platform.runLater
		// is not used, the call to show() will cause the worker to get
		// blocked during notification and it will prevent the worker
		// from performing any additional notification for state changes.
		//
		// Sine the dialog is hidden as a result of a change in worker
		// state, calling show() without wrapping it in Platform.runLater
		// will cause the progress dialog to run forever when the dialog
		// is attached to workers that start out with a state of READY.
		//
		// This also creates a case where the worker's state can change
		// to finished before the dialog is shown, resulting in an
		// an attempt to hide the dialog before it is shown. It's
		// necessary to track whether or not this occurs, so flags are
		// set to indicate if the dialog is visible and if if the call
		// to show should still be allowed.
		cancelDialogShow = false;

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (!cancelDialogShow)
				{
					progressBar.progressProperty().bind(
							worker.progressProperty());
					dialogVisible = true;
					dialog.show();
				}
			}
		});
	}

	private void end()
	{
		progressBar.progressProperty().unbind();
		dialogVisible = false;
		dialog.hide();
		dialog.close();
	}

	@Override
	protected void layoutChildren()
	{
		if (progressBar != null)
		{
			Insets insets = getInsets();
			double w = getWidth() - insets.getLeft() - insets.getRight();
			double h = getHeight() - insets.getTop() - insets.getBottom();

			double prefH = progressBar.prefHeight(-1);
			double x = insets.getLeft() + (w - w) / 2.0;
			double y = insets.getTop() + (h - prefH) / 2.0;

			progressBar.resizeRelocate(x, y, w, prefH);
		}
	}
}