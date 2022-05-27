import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {


    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();

        //объект запроса
        String api = "M4ZBehhq8o3f1Taatsp2k3n8tN8buw1nWGBRSaIy";
        HttpGet requestBody = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=" + api);

        //удаленный сервис
        CloseableHttpResponse responseBody = httpClient.execute(requestBody);

        //Печать ответа
        String body = new String(responseBody.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        //System.out.println(body);

        //парсим из тела ответа объекта и печатаем
        NasaPost post = mapper.readValue(body, NasaPost.class);
        responseBody.close();

        //System.out.println(post);
        String imageUrl = post.getHDurl();

        //определяем имя файла
        String[] arr = imageUrl.split("/");
        String fileName = arr[arr.length - 1];   //последний элемент массива

        //создаем файл
        File fileImage = new File(fileName);
        fileImage.createNewFile();

        //запрос изображения
        HttpGet requestUrlImage = new HttpGet(imageUrl);
        CloseableHttpResponse responseUrlImage = httpClient.execute(requestUrlImage);
        HttpEntity entity = responseUrlImage.getEntity();

        if (entity != null) {
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileImage))) {
                entity.writeTo(bufferedOutputStream);
                System.out.println("\nФайл " + fileName + " создан");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("\nФайл " + fileName + " не создан");
        }

        responseUrlImage.close();
        httpClient.close();

    }

}
