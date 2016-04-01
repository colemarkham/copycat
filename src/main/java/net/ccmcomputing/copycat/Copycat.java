package net.ccmcomputing.copycat;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

/**
 * Slackcat! A Slack webhook that listens for HTTP status codes and responds
 * with an appropriate image from http.cat.
 *
 */
public class Copycat{

   private static String getCatApiUrl(){
      String url = "http://thecatapi.com/api/images/get?format=src";
      String apiKey = getCatApiKey();
      if(apiKey != null) {
         url += "&api_key=" + apiKey;
      }
      return url;
   }

   static String getCatApiKey(){
      ProcessBuilder processBuilder = new ProcessBuilder();
      return processBuilder.environment().get("CATAPI_KEY");
   }

   static int getHerokuAssignedPort(){
      ProcessBuilder processBuilder = new ProcessBuilder();
      if(processBuilder.environment().get("PORT") != null) return Integer.parseInt(processBuilder.environment().get("PORT"));
      return 4567; // return default port if heroku-port isn't set (i.e. on
                   // localhost)
   }

   public static SlackMessage handleMessage(String user, String incomingText){
      if(user.toLowerCase().contains("bot")) return new SlackMessage(null);
      LocalDate today = LocalDate.now();
      if(today.getMonth() == Month.APRIL && today.getDayOfMonth() == 1) {
         try (CloseableHttpClient httpclient = HttpClients.createDefault();){
            HttpHead request = new HttpHead(getCatApiUrl());
            try (CloseableHttpResponse response = httpclient.execute(request)){
               Header locationHeader = response.getFirstHeader("Location");
               if(locationHeader != null) {
                  String catUrl = locationHeader.getValue();
                  return new SlackMessage(incomingText + "\n" + catUrl);
               }
            }
         }catch(IOException e){
            e.printStackTrace();
         }
         // Ok, no cat, just the message
         return new SlackMessage(incomingText);
      }
      return new SlackMessage(null);
   }

   public static void main(String[] args){
      Gson gson = new Gson();
      port(getHerokuAssignedPort());
      get("/", (req, res) -> "This is a webhook. See https://github.com/colemarkham/copycat for details.");
      post("/", (req, res) -> handleMessage(req.queryParams("user_name"), req.queryParams("text")), gson::toJson);
   }

   public static class SlackMessage{
      String text;
      String username;

      public SlackMessage(String text){
         this.text = text;
      }

   }

}
