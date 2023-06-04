/**
 * Copyright 2022 Zhang Guanhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.artbits.quickio.core;

final class Constants {
    final static String DB_PATH = "data/db/";
    final static String KV_PATH = "data/kv/";
    final static String TIN_PATH = "data/tin/";
    final static String INDEX = "index";

    final static String ILLEGAL_NAME = "The name cannot be null or empty";
    final static String SPECIAL_CHARACTER_NAME = "The name cannot contain \"/\"";
    final static String INDEX_ALREADY_EXISTS = " index already exists";
    final static String NON_INDEXED_FIELD = "Non indexed field";
    final static String FIELD_DOES_NOT_EXIST = "This field does not exist";
    final static String FIELD_DOES_NOT_SUPPORT_SORTING = "This field does not support sorting";
    final static String SORTING_FIELD_NAME_ILLEGAL = "The sort method field name cannot be null or empty";
    final static String SORTING_PARAMETER_VALUE_ILLEGAL = "The sorting parameter value can only be 1 or -1";
    final static String KEY_ALREADY_EXISTS_AND_NOT_AVAILABLE = "The new key already exists and is not available";
    final static String FIELD_NOT_NUMERICAL_TYPE = "This field is not of numerical type";
}