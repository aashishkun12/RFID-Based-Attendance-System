#include <ESP8266WiFi.h>
#include <ArduinoHttpClient.h>
#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN D8
#define RST_PIN D3

WiFi Credentials
const char* ssid = " Your SSID ";
const char* password = " Your PASSWORD ";

// Server Details
const char* server = " Your Server IP ";
int port = 80;

MFRC522 mfrc522(SS_PIN, RST_PIN);

WiFiClient wifi;
HttpClient client = HttpClient(wifi, server, port);

void setup() {
    Serial.begin(9600);
    delay(2000);
    SPI.begin();
    mfrc522.PCD_Init();

    // Wifi Connection
    WiFi.begin(ssid, password);
    Serial.print("\nConnecting to WiFi...");
    while(WiFi.status() != WL_CONNECTED){
      delay(1500);
      Serial.print(".");
    }
    Serial.println("\nConnected to WiFi Network");
    Serial.println("Scan a RFID card/ tag: ");
}

void loop() {

  if(mfrc522.PICC_IsNewCardPresent()){
    if(mfrc522.PICC_ReadCardSerial()){
      String rfid = "";
      for(byte i = 0; i < mfrc522.uid.size; i++){
        rfid += String(mfrc522.uid.uidByte[i], HEX);
      }
      rfid.toUpperCase();
      Serial.println("Scanned RFID: " + rfid);    
      mfrc522.PICC_HaltA();

      String postData = "rfid=" + rfid;
      String contentType = "application/x-www-form-urlencoded";

      // Attempt to connect to the server
      Serial.println("Attempting to connect to server...");
      if(client.connect(server, 80)){
        client.post("/attendance.php", contentType, postData);

        // Get response
        int statusCode = client.responseStatusCode();
        String response = client.responseBody();

        Serial.print("Status Code: " + String(statusCode));
        Serial.print("\nResponse: " + response + "\n");
        Serial.print("\n\n-------------------------------------\n\n");
        delay(5000); // Wait for 5 seconds before sending another request
        Serial.println("Scan a RFID card/ tag: ");
      }
      else{
        Serial.println("Error: Cannot connect to server.");
        Serial.print("\n\n-------------------------------------\n\n");
        delay(3000);
        Serial.println("Scan a RFID card/ tag: ");
      }
    }
  }
}
