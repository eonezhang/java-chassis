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
package io.servicecomb.provider.springmvc.reference;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.xml.ws.Holder;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.servicecomb.common.rest.RestEngineSchemaListener;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.swagger.invocation.Response;

public class TestCseClientHttpRequest {
    @RequestMapping(path = "SpringmvcImpl")
    static class SpringmvcImpl {
        @RequestMapping(path = "/bytes", method = RequestMethod.POST)
        public byte[] bytes(@RequestBody byte[] input) {
            input[0] = (byte) (input[0] + 1);
            return input;
        }
    }

    @Test
    public void test() throws IOException {
        UnitTestMeta meta = new UnitTestMeta();

        CseContext.getInstance()
                .getSchemaListenerManager()
                .setSchemaListenerList(Arrays.asList(new RestEngineSchemaListener()));

        SchemaMeta schemaMeta = meta.getOrCreateSchemaMeta(SpringmvcImpl.class);
        CseContext.getInstance().getSchemaListenerManager().notifySchemaListener(schemaMeta);

        Holder<Invocation> holder = new Holder<>();
        CseClientHttpRequest client =
            new CseClientHttpRequest(URI.create("cse://test/" + SpringmvcImpl.class.getSimpleName() + "/bytes"),
                    HttpMethod.POST) {

                /**
                 * {@inheritDoc}
                 */
                @Override
                protected Response doInvoke(Invocation invocation) {
                    holder.value = invocation;
                    return Response.ok("result");
                }
            };
        byte[] body = "abc".getBytes();
        client.setRequestBody(body);

        client.execute();
        client.close();

        Assert.assertArrayEquals(body, holder.value.getSwaggerArgument(0));
    }
}
