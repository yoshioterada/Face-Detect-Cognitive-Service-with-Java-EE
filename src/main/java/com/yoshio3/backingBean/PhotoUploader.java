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
package com.yoshio3.backingBean;

import com.yoshio3.services.StorageService;
import com.yoshio3.services.EmotionService;
import com.yoshio3.services.FaceDetectService;
import com.yoshio3.services.entities.EmotionResponseJSONBody;
import com.yoshio3.services.entities.FaceDetectResponseJSONBody;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.primefaces.event.CaptureEvent;

/**
 *
 * @author Yoshio Terada
 */
@Named(value = "photoup")
@RequestScoped
public class PhotoUploader implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PhotoUploader.class.getName());

    //Please change following 4 lines (Blob URL)    
    protected final static String UPLOAD_DIRECTORY_NAME_OF_BLOB = "uploaded";
    private final static String AZURE_BLOG_UPLOAD_URL = "https://yoshiofileup.blob.core.windows.net/" + UPLOAD_DIRECTORY_NAME_OF_BLOB + "/";
    private final static String EMOTIONAL_API_SUBSCRIPTION = "********************************";
    private final static String FACE_API_SUBSCRIPTION = "********************************";

    private final static String IMAGE_FORMAT_EXTENSION = ".jpg";
    
    private Double anger;
    private Double contempt;
    private Double disgust;
    private Double fear;
    private Double happiness;
    private Double neutral;
    private Double sadness;
    private Double surprise;
    private Double age;
    private String gender;

    @Inject
    private StorageService storageService;

    @Inject
    EmotionService emotionService;

    @Inject
    FaceDetectService faceDetectService;

    private String fileURL;

    public void sendPhoto(CaptureEvent captureEvent) {
        try {
            //ファイル名の作成
            UUID uuid = UUID.randomUUID();
            String fileName = uuid.toString() + IMAGE_FORMAT_EXTENSION;
            byte[] data = captureEvent.getData();

            //Azure Storage にファイルのアップロード
            storageService.uploadFile(data, fileName);
            //アップロードされたファイルの URL
            fileURL = AZURE_BLOG_UPLOAD_URL + fileName;

            //Async invocation of Face API & Emotional API (Speed up)
            Future<Response> responseForEmotion = emotionService.getEmotionalInfo(fileURL, EMOTIONAL_API_SUBSCRIPTION);
            Future<Response> responseForFace = faceDetectService.getFaceInfo(fileURL, FACE_API_SUBSCRIPTION);

            //Emotional API の結果を取得
            Response emotionRes = responseForEmotion.get();
            jobForEmotion(emotionRes);
            //Face API の結果を取得
            Response faceRes = responseForFace.get();
            jobForFace(faceRes);
        } catch (InterruptedException | ExecutionException | IllegalStateException ex) {
            Logger.getLogger(PhotoUploader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void jobForEmotion(Response emotionRes) {
        EmotionResponseJSONBody[] persons = null;
        if (checkRequestSuccess(emotionRes)) {
            persons = emotionRes.readEntity(EmotionResponseJSONBody[].class);
        } else {
            handleIllegalState(emotionRes);
        }
        if (persons == null || persons.length < 1) {
            return;
        }
        //現在は一人のみ解析処理
        EmotionResponseJSONBody emotionalPerson = persons[0];
        Map<String, Object> scores = emotionalPerson.getScores();

        //感情の情報を取得
        anger = (Double) scores.get("anger");
        contempt = (Double) scores.get("contempt");
        disgust = (Double) scores.get("disgust");
        fear = (Double) scores.get("fear");
        happiness = (Double) scores.get("happiness");
        neutral = (Double) scores.get("neutral");
        sadness = (Double) scores.get("sadness");
        surprise = (Double) scores.get("surprise");
    }

    private void jobForFace(Response faceRes) {
        FaceDetectResponseJSONBody[] persons = null;
        if (checkRequestSuccess(faceRes)) {
            persons = faceRes.readEntity(FaceDetectResponseJSONBody[].class);
        } else {
            handleIllegalState(faceRes);
        }
        if (persons == null || persons.length < 1) {
            return;
        }
        //現在は一人のみ解析処理
        FaceDetectResponseJSONBody faceDetectData = persons[0];

        //年齢、性別を取得
        Map<String, Object> faceAttributes = faceDetectData.getFaceAttributes();
        age = (Double) faceAttributes.get("age");
        gender = (String) faceAttributes.get("gender");

        //画像の場所、枠の大きさの取得 : TODO
        faceDetectData.getFaceRectangle().forEach((key, value) -> {
            Integer top, left, width, height;
            switch (key) {
                case "top":
                    top = (Integer) value;
                    break;
                case "left":
                    left = (Integer) value;
                case "width":
                    width = (Integer) value;
                case "height":
                    height = (Integer) value;
            }
        });
    }

    /*
     REST 呼び出し成功か否かの判定
     */
    protected boolean checkRequestSuccess(Response response) {
        Response.StatusType statusInfo = response.getStatusInfo();
        Response.Status.Family family = statusInfo.getFamily();
        return family != null && family == Response.Status.Family.SUCCESSFUL;
    }

    /*
     REST 呼び出しエラー時の処理
     */
    protected void handleIllegalState(Response response) throws IllegalStateException {
        String error = response.readEntity(String.class
        );
        LOGGER.log(Level.SEVERE, "{0}", error);
        throw new IllegalStateException(error);
    }

    /* 画像に枠をつけたい */
    private void createNewImage(String url, Integer top, Integer left, Integer width, Integer height) throws IOException {
        Image image = new Image(url, true);
        double canvWidth = image.getWidth();
        double canvHeight = image.getHeight();

        Group root = new Group();
        Scene s = new Scene(root, canvWidth, canvHeight, Color.WHITE);

        final Canvas canvas = new Canvas(canvWidth, canvHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(image, 0, 0);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(left, top, width, height);

        WritableImage writableImage = new WritableImage((int) canvWidth, (int) canvHeight);
        canvas.snapshot(null, writableImage);
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
        OutputStream output = null;
        ImageIO.write(renderedImage, "jpg", new File("/tmp/hoge.jpg"));
    }

    /* パーセント表示のためにデータをコンバート */
    private Double convert(Double before) {
        if (before == null) {
            return before;
        }
        String after = String.format("%.2f", before);
        return Double.valueOf(after) * 100;

    }

    public String getGender() {
        if (gender == null) {
            return gender;
        } else if (gender.equals("male")) {
            return "男性";
        } else {
            return "女性";
        }
    }

    public String getFileURL() {
        return fileURL;
    }

    /**
     * @return the anger
     */
    public Double getAnger() {
        return convert(anger);
    }

    /**
     * @return the contempt
     */
    public Double getContempt() {
        return convert(contempt);
    }

    /**
     * @return the disgust
     */
    public Double getDisgust() {
        return convert(disgust);
    }

    /**
     * @return the fear
     */
    public Double getFear() {
        return convert(fear);
    }

    /**
     * @return the happiness
     */
    public Double getHappiness() {
        return convert(happiness);
    }

    /**
     * @return the neutral
     */
    public Double getNeutral() {
        return convert(neutral);
    }

    /**
     * @return the sadness
     */
    public Double getSadness() {
        return convert(sadness);
    }

    /**
     * @return the surprise
     */
    public Double getSurprise() {
        return convert(surprise);
    }

    public Double getAge() {
        return age;
    }
}
