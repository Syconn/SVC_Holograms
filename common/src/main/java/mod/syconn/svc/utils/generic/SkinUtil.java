package mod.syconn.svc.utils.generic;

public class SkinUtil { /// TODO DO I NEED?



//    public static boolean getModelType(String name){
//        String id = convertUsernameToUUID(name);
//        if (!id.isEmpty()) {
//            try {
//                HttpGet request = new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + id);
//                CloseableHttpClient client = HttpClients.createDefault();
//                CloseableHttpResponse response = client.execute(request);
//                HttpEntity entity = response.getEntity();
//                JsonObject jsonObject = (JsonObject) JsonParser.parseString(EntityUtils.toString(entity));
//
//                if (jsonObject != null){
//                    String bitcode = jsonObject.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
//                    byte[] decodedBytes = Base64.decodeBase64(bitcode.getBytes());
//                    JsonObject SkinData = (JsonObject) JsonParser.parseString(new String(decodedBytes));
//                    return SkinData.getAsJsonObject("textures").getAsJsonObject("SKIN").has("metadata");
//                }
//            }
//            catch (IOException e) {
//                log.error("e: ", e);
//            }
//        }
//        return false;
//    }
}
