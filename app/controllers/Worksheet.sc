val x = 10
val k = 2
val iloscKul = 3

var suma = 0.0
var mnożnik = 1000
do {
  suma = k*mnożnik * (1- Math.pow(2,iloscKul)) / (-1)
  mnożnik/=2
} while(suma > x*1000)

println(suma)