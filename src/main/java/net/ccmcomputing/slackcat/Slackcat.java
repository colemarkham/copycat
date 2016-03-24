package net.ccmcomputing.slackcat;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

/**
 * Slackcat! A Slack webhook that listens for HTTP status codes and responds
 * with an appropriate image from http.cat.
 *
 */
public class Slackcat{
   public static final int[] HTTP_CAT_CODES = {100, 101, 200, 201, 202, 204, 206, 207, 300, 301, 302, 303, 304, 305, 307, 400, 401, 402, 403, 404, 405, 406, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 422, 423, 424, 425, 426, 429, 431, 444,
                  450, 451, 500, 502, 503, 506, 507, 508, 509, 599};
   private static final Pattern PATTERN = Pattern.compile("(?<![0-9,.$])(?<!http.cat/)[1-5]\\d{2}(?![0-9,.$])");

   static int getHerokuAssignedPort(){
      ProcessBuilder processBuilder = new ProcessBuilder();
      if(processBuilder.environment().get("PORT") != null) return Integer.parseInt(processBuilder.environment().get("PORT"));
      return 4567; // return default port if heroku-port isn't set (i.e. on
                   // localhost)
   }

   public static SlackMessage handleMessage(String incomingText){
      Matcher matcher = PATTERN.matcher(incomingText);
      if(matcher.find()) {
         String match = matcher.group();
         try{
            int code = Integer.parseInt(match);
            if(Arrays.binarySearch(HTTP_CAT_CODES, code) >= 0) return new SlackMessage("https://http.cat/" + match);
         }catch(NumberFormatException e){
            e.printStackTrace();
         }
      }
      return new SlackMessage(null);
   }

   public static void main(String[] args){
      Gson gson = new Gson();
      port(getHerokuAssignedPort());
      get("/", (req, res) -> "This is a webhook. See https://github.com/colemarkham/slackcat for details.");
      post("/", (req, res) -> handleMessage(req.queryParams("text")), gson::toJson);
   }

   public static class SlackMessage{
      String text;
      String username;

      public SlackMessage(String text){
         this.text = text;
      }

   }

}
