/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mfcoding.locationBP.services;

/**
 * {@inheritDoc}
 * Extends the {@link PlacesUpdateService} to force Intent redelivery 
 * on Eclaire+ devices (where this defaults to false).
 */
public class EclairPlacesUpdateService extends LocationUpdateService {
  @Override
  protected void setIntentRedeliveryMode(boolean enable) {
    setIntentRedelivery(true);
  }
}