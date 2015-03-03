
int xPin = 0;                 
int yPin = 1;                 
int x = 0;                  
int y = 0;    

void setup() {  
  Serial.begin(9600);
}

int treatValue(int data) { 
  return (data * 7 / 512) + 30;
}

void ubication(int x, int y){
  if((x<=38 && x>=35) && y > 37 ) //north
    Serial.println("n"); 
  if((x<=38 && x>=35) && y <36 ) //south
    Serial.println("s");      
  if((y<=38 && y>=35) && x>37   ) //east
    Serial.println("e");
  if((y<=38 && y>=35) && x<36  ) //west
    Serial.println("w");    
  if(x>38 && (y>38 ) ) //nort east
    Serial.println("ne");
  if(x<35 && y>38 ) //nort west
    Serial.println("nw"); 
  if(x>38 && (y<35 ) ) //south east
    Serial.println("se"); 
  if(x<35 && y<35 ) //south west
    Serial.println("sw");     
 /* if(x<=37 && x>=36 && y<=37 && y>=36 )//middle point
    Serial.println("00"); */
}
void loop() {

  x = treatValue( analogRead(xPin));
  y = treatValue(analogRead(yPin)); 
  ubication(x, y); 
  delay(20); 
}





