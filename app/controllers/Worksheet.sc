

def wendif( warunek : => Boolean )( blok : => Unit ) = {
  if(warunek) blok
}


wendif{3 < 2} {
  println("Dukes chuj")
}
