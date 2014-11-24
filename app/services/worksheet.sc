val sredniaPensjaUrz = 4300.0
val iloscUrz =  443000.0
val iloscPodatnikow = 23804299 + 521600.0
val iloscOsobPelnoletnich = 38496000.0
val stopaBezrobocia = 0.12

val ileProcentOdprowadza =
  iloscPodatnikow / iloscOsobPelnoletnich

val kwotaWolna = 3091.0
val sredniaPensja = 3900.0

val wydajnoscUrzedowa = iloscPodatnikow / (iloscUrz + .0)
val kwotaNaUrz = iloscPodatnikow * sredniaPensjaUrz

val sumaPod = Math.round(12.0 * iloscPodatnikow.asInstanceOf[Long] * (sredniaPensja) * 0.22)

val procentNaPodatnikow = kwotaNaUrz/sumaPod

val a = Stream.from(3300, 10)
print(a.take(10).toList)

