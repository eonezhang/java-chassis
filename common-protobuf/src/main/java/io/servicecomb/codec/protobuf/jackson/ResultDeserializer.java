/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.codec.protobuf.jackson;

import java.util.Map;

import io.servicecomb.codec.protobuf.codec.AbstractFieldCodec.ReaderHelpData;

public class ResultDeserializer extends AbstractDeserializer {

    public ResultDeserializer(Map<String, ReaderHelpData> readerHelpDataMap) {
        super(readerHelpDataMap);
    }

    @Override
    protected Object createResult() {
        return null;
    }

    @Override
    protected Object updateResult(Object result, Object value, ReaderHelpData helpData) {
        return value;
    }
}
