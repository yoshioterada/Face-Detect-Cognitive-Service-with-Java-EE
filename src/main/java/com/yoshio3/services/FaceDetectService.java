/*
* Copyright 2016 Yoshio Terada
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
package com.yoshio3.services;

import com.yoshio3.services.entities.FaceDetectRequestJSONBody;
import com.yoshio3.services.entities.utils.MyObjectMapperProvider;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author Yoshio Terada
 */
@Dependent
public class FaceDetectService implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(FaceDetectService.class.getName());
    private static final String BASE_URI = "https://api.projectoxford.ai/face/v1.0/detect?returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=age,gender";

    /*
        対応ォーマット： JPEG, PNG, GIF(最初のフレーム), BMP
        画像サイズ： 4MB 以下
        検知可能な顔のサイズ：36x36 〜 4096x4096
        一画像辺り検知可能な人数：64 名
        指定可能な属性オプション(まだ実験的不正確)：
            age, gender, headPose, smile and facialHair, and glasses
            HeadPose の pitch 値は 0 としてリザーブ
     */

    public Future<Response> getFaceInfo(String pictURI, String subsctiption) throws InterruptedException, ExecutionException {
        Client client = ClientBuilder.newBuilder()
                .register(MyObjectMapperProvider.class)
                .register(JacksonFeature.class)
                .build();

        WebTarget target = client.target(BASE_URI);
        FaceDetectRequestJSONBody entity = new FaceDetectRequestJSONBody();
        entity.setUrl(pictURI);

        Future<Response> response = target
                .request(MediaType.APPLICATION_JSON)
                .header("Ocp-Apim-Subscription-Key", subsctiption)
                .async()
                .post(Entity.entity(entity, MediaType.APPLICATION_JSON_TYPE));
        return response;
    }
}
