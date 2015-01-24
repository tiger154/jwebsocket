
//variables of the Leds demostration application
int blue   = 12;
int red    = 8;
int green  = 7;
int yellow = 4;
int data   = -1;
int ledState = -1;
boolean onBlue   = false;
boolean onRed    = false;
boolean onGreen  = false;
boolean onYellow = false;

//variables of the joystick demostration application
int xPin = 0;                 
int yPin = 1;   
int oldX = 0;
int oldY = 0;
int x = 0;                  
int y = 0; 


void setup(){
  Serial.begin(9600);
  pinMode(blue,OUTPUT);
  pinMode(red,OUTPUT);
  pinMode(green,OUTPUT);
  pinMode(yellow,OUTPUT);
}

void loop(){
  if(Serial.available() > 0){
    //capture the data from the USB port
    data = Serial.read();
  }   
  //ledsPrgram  
  switch(data){
  case 49:
    if(!onBlue){
      digitalWrite(blue,HIGH);
      onBlue = true;
      changeLedState();       
      Serial.println("L/"+String(ledState));

    }
    else {
      digitalWrite(blue,LOW);        
      onBlue = false;
      changeLedState();       
      Serial.println("L/"+String(ledState));

    }
    break;
  case 50:    
    if(!onRed){
      digitalWrite(red,HIGH);       
      onRed = true;
      changeLedState();       
      Serial.println("L/"+String(ledState));
    }
    else {
      digitalWrite(red,LOW);        
      onRed = false;
      changeLedState();       
      Serial.println("L/"+String(ledState));
    }

    break;
  case 51:

    if(!onGreen) {
      digitalWrite(green,HIGH);       
      onGreen = true;
      changeLedState();       
      Serial.println("L/"+String(ledState));
    }
    else  {
      digitalWrite(green,LOW);        
      onGreen = false;
      changeLedState();       
      Serial.println("L/"+String(ledState));
    } 
    break;
  case 52:
    if(!onYellow) {
      digitalWrite(yellow,HIGH);  
      onYellow = true;
      changeLedState();       
      Serial.println("L/"+String(ledState));
    }
    else {
      digitalWrite(yellow,LOW);       
      onYellow = false;
      changeLedState();       
      Serial.println("L/"+String(ledState));
    } 
    break; 
  case 53:    
    Serial.println("L/"+String(ledState));
    break; 
  } 

  //Joystick program

  x = treatValue( analogRead(xPin));
  y = treatValue(analogRead(yPin));   
  changePosition(x, y); 
  data = -1; 

}

//--functions------------------
void changeLedState()
{
  if(onBlue && onRed && onGreen && onYellow)
    ledState = 15;  
  if(onBlue && onRed && onGreen && !onYellow)
    ledState = 14; 
  if(onBlue && onRed && !onGreen && onYellow)
    ledState = 13;
  if(onBlue && onRed && !onGreen && !onYellow)
    ledState = 12;  
  if(onBlue && !onRed && onGreen && onYellow)
    ledState = 11;     
  if(onBlue && !onRed && onGreen && !onYellow)
    ledState = 10;
  if(onBlue && !onRed && !onGreen && onYellow)
    ledState = 9; 
  if(onBlue && !onRed && !onGreen && !onYellow)
    ledState = 8; 
  if(!onBlue && onRed && onGreen && onYellow)
    ledState = 7; 
  if(!onBlue && onRed && onGreen && !onYellow)
    ledState = 6;  
  if(!onBlue && onRed && !onGreen && onYellow)
    ledState = 5;   
  if(!onBlue && onRed && !onGreen && !onYellow)
    ledState = 4;
  if(!onBlue && !onRed && onGreen && onYellow)
    ledState = 3;
  if(!onBlue && !onRed && onGreen && !onYellow)
    ledState = 2;
  if(!onBlue && !onRed && !onGreen && onYellow)
    ledState = 1;
  if(!onBlue && !onRed && !onGreen && !onYellow)
    ledState = 0;
}

int treatValue(int data) { 
  return (data * 7 / 512) + 30;
}

void changePosition(int x, int y){
  if (x == 43) {
    x = 42;
  }
  x= x-36;
  y= y-36;
  if(x!= oldX || y!=oldY){
    Serial.print("J/");
    Serial.print(x);
    Serial.print("_");
    Serial.print(y);
    Serial.println();
    oldX = x;
    oldY = y;
  }
}


