int blue   = 12;
int red    = 8;
int green  = 7;
int yellow = 4;
int cond   = -1;
char state = '#';
boolean onBlue   = false;
boolean onRed    = false;
boolean onGreen  = false;
boolean onYellow = false;

//-----------------------------------------
void changeLedState()
{
  if(onBlue && onRed && onGreen && onYellow)
    state = 'a';  
  if(onBlue && onRed && onGreen && !onYellow)
    state = 'b'; 
  if(onBlue && onRed && !onGreen && onYellow)
    state = 'c';
  if(onBlue && onRed && !onGreen && !onYellow)
    state = 'd';  
  if(onBlue && !onRed && onGreen && onYellow)
    state = 'e';     
  if(onBlue && !onRed && onGreen && !onYellow)
    state = 'f';
  if(onBlue && !onRed && !onGreen && onYellow)
    state = 'g'; 
  if(onBlue && !onRed && !onGreen && !onYellow)
    state = 'h'; 
  if(!onBlue && onRed && onGreen && onYellow)
    state = 'i'; 
  if(!onBlue && onRed && onGreen && !onYellow)
    state = 'j';  
  if(!onBlue && onRed && !onGreen && onYellow)
    state = 'k';   
  if(!onBlue && onRed && !onGreen && !onYellow)
    state = 'l';
  if(!onBlue && !onRed && onGreen && onYellow)
    state = 'm';
  if(!onBlue && !onRed && onGreen && !onYellow)
    state = 'n';
  if(!onBlue && !onRed && !onGreen && onYellow)
    state = 'o';
  if(!onBlue && !onRed && !onGreen && !onYellow)
    state = 'p';
}
//---------------------------------------------


void setup()
{
  Serial.begin(9600);
  pinMode(blue,OUTPUT);
  pinMode(red,OUTPUT);
  pinMode(green,OUTPUT);
  pinMode(yellow,OUTPUT);
}

void loop(){
  if (Serial.available() > 0) { 

   
    cond = Serial.read();
    switch(cond){

    case 49:
      if(!onBlue)
      {
        digitalWrite(blue,HIGH);
        onBlue = true;
        changeLedState();       
        Serial.print(state);

      }
      else
      {
        digitalWrite(blue,LOW);        
        onBlue = false;
        changeLedState();       
        Serial.print(state);

      }            

      break;

    case 50:    
      if(!onRed)
      {
        digitalWrite(red,HIGH);       
        onRed = true;
        changeLedState();       
        Serial.print(state);

      }
      else
      {
        digitalWrite(red,LOW);        
        onRed = false;
        changeLedState();       
        Serial.print(state);
      } 

      break;
    case 51:

      if(!onGreen)
      {
        digitalWrite(green,HIGH);       
        onGreen = true;
        changeLedState();       
        Serial.print(state);

      }
      else
      {
        digitalWrite(green,LOW);        
        onGreen = false;
        changeLedState();       
        Serial.print(state);

      } 

      break;
    case 52:
      if(!onYellow)
      {
        digitalWrite(yellow,HIGH);  
        onYellow = true;
        changeLedState();       
        Serial.print(state);

      }
      else
      {
        digitalWrite(yellow,LOW);       
        onYellow = false;
        changeLedState();       
        Serial.print(state);

      }    

      break; 
    case 53:    
      Serial.print(state);
      break;

      default:
      {        
        Serial.print("I received: ");
        Serial.println(cond);
        Serial.print(" millis ");               
        
        Serial.println();
      }

    }

  }
}










