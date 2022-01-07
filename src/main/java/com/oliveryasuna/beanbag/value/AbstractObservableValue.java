/*
 * Copyright 2022 Oliver Yasuna
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.oliveryasuna.beanbag.value;

import com.oliveryasuna.beanbag.ObservableBean;
import com.oliveryasuna.beanbag.value.event.ValueChangedEvent;
import com.oliveryasuna.beanbag.value.event.ValueSetReadOnlyFailedEvent;
import com.oliveryasuna.beanbag.value.listener.ValueChangedListener;
import com.oliveryasuna.beanbag.value.listener.ValueSetReadOnlyFailedListener;
import com.oliveryasuna.commons.language.pattern.registry.Registration;
import org.apache.commons.lang3.event.EventListenerSupport;

public abstract class AbstractObservableValue<T, SUB extends AbstractObservableValue<T, SUB>> extends ObservableBean<T, SUB> {

  // Constructors
  //--------------------------------------------------

  protected AbstractObservableValue(final T value, final boolean readOnly) {
    super(value);

    this.readOnly = readOnly;
  }

  protected AbstractObservableValue(final T value) {
    this(value, false);
  }

  // Miscellaneous fields
  //--------------------------------------------------

  protected boolean readOnly;

  // Listener registries
  //--------------------------------------------------

  protected final EventListenerSupport<ValueChangedListener> valueChangedListeners = EventListenerSupport.create(ValueChangedListener.class);

  protected final EventListenerSupport<ValueSetReadOnlyFailedListener> valueSetReadOnlyFailedListeners =
      EventListenerSupport.create(ValueSetReadOnlyFailedListener.class);

  // Listener registration methods
  //--------------------------------------------------

  public Registration addValueChangedListener(final ValueChangedListener<T, SUB> listener) {
    valueChangedListeners.addListener(listener);

    return (() -> valueChangedListeners.removeListener(listener));
  }

  public void removeValueChangedListener(final ValueChangedListener<T, SUB> listener) {
    valueChangedListeners.removeListener(listener);
  }

  public void addValueSetReadOnlyFailedListener(final ValueSetReadOnlyFailedListener<T, SUB> listener) {
    valueSetReadOnlyFailedListeners.addListener(listener);
  }

  public void removeValueSetReadOnlyFailedListener(final ValueSetReadOnlyFailedListener<T, SUB> listener) {
    valueSetReadOnlyFailedListeners.removeListener(listener);
  }

  // Listener dispatch methods
  //--------------------------------------------------

  protected void fireValueChangedEvent(final T newValue, final T oldValue) {
    valueChangedListeners.fire().valueChanged(new ValueChangedEvent<>(newValue, oldValue, (SUB)this));
  }

  protected void fireValueSetReadOnlyFailedEvent(final T candidateValue, final T value) {
    valueSetReadOnlyFailedListeners.fire().valueSetReadOnlyFailed(new ValueSetReadOnlyFailedEvent<>(candidateValue, value, (SUB)this));
  }

  // Getters/setters
  //--------------------------------------------------

  public boolean isReadOnly() {
    return readOnly;
  }

  protected void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  // Value methods
  //--------------------------------------------------

  public T getValue() {
    return getBean();
  }

  public void setValue(final T value) {
    if(isReadOnly()) {
      try {
        throw new IllegalStateException("Value is read-only.");
      } finally {
        fireValueSetReadOnlyFailedEvent(value, getValue());
      }
    }

    final T oldValue = getValue();

    setBean(value);

    if(value != oldValue) {
      fireValueChangedEvent(value, oldValue);
    }
  }

}
