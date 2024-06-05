/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function toggleFilter(e) {
  var filterElement = e.target.closest("th").querySelector(".table-header-filter");
  filterElement.style.display = filterElement.style.display == "none" ? "block" : "none";
  event.preventDefault();
}

function toggleSort(e) {
  var filterElement = e.target.closest("th").querySelector(".table-header-filter input");
  var fieldName = filterElement.name;
  var fieldValue = filterElement.value;

  var queryParams = new URLSearchParams(window.location.search);
  var existing = queryParams.get("sort");
  var prefix = existing == null || decodeURIComponent(existing).startsWith("desc-") ? "asc" : "desc"
  queryParams.set("sort", encodeURIComponent(prefix + "-" + fieldName));
  location.search = queryParams.toString();

  event.preventDefault();
}

function filter(e) {
  var filterElement = e.target.closest("th").querySelector(".table-header-filter input");
  var fieldName = filterElement.name;
  var fieldValue = filterElement.value;

  var queryParams = new URLSearchParams(window.location.search);
  queryParams.set("filter", encodeURIComponent(fieldName + "=" + fieldValue));
  location.search = queryParams.toString();

  event.preventDefault();
}
