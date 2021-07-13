import java.lang.Exception
import kotlin.math.abs

class Customer {
    private val card: Card = Card()
    private val journey: Journey = Journey()

    fun loadCard(amount: Float) {
        card.load(amount)
    }

    fun checkCardBalance(): Float {
        runUnfinishedTripChecker()
        return card.balance
    }

    fun startTrip(trip: Trip) {
        runUnfinishedTripChecker()
        if (card.balance >= FareTag.MAX.fare) {
            journey.addTrip(trip)
        } else throw NotEnoughBalanceException()
    }

    fun endTrip(trip: Trip, station: Station) {
        journey.endTrip(trip, station)
        println("End Trip (${trip.entryStation}->$station) = ${trip.fareTag.fare}")
        card.deduct(trip.fareTag.fare)
    }

    private fun runUnfinishedTripChecker() {
        val transportMode = journey.transModeForTripNotEnded()
        if (transportMode == TransportMode.TUBE) {
            println("Tube trip didn't end -- deduct max fare")
            card.deduct(FareTag.MAX.fare)
        } else if (transportMode == TransportMode.BUS) {
            println("Bus trip didn't end -- deduct bus fare")
            card.deduct(FareTag.BUS.fare)
        }
    }
}

class NotEnoughBalanceException : Exception()
class Journey {
    private val listOfTrips = ArrayList<Trip>()
    private var isStarted: Boolean = false
    fun transModeForTripNotEnded(): TransportMode {
        if (isStarted) {
            listOfTrips.forEach {
                if (!it.exit) {
                    removeOpenTrip(it)
                    return it.mode
                }
            }
        }
        return TransportMode.NONE
    }

    fun addTrip(trip: Trip) {
        if (!isStarted) isStarted = true
        listOfTrips.add(trip)
    }

    private fun removeOpenTrip(trip: Trip) {
        listOfTrips.remove(trip)
    }

    fun endTrip(trip: Trip, exitStation: Station) {
        if (listOfTrips.contains(trip)) {
            trip.exit(exitStation)
        }
    }
}

class Card {
    var balance: Float = 0.0f
    fun load(amount: Float) {
        balance += amount
    }

    fun deduct(amount: Float) {
        balance -= amount
    }
}

class Trip(val mode: TransportMode, val entryStation: Station) {
    var fareTag: FareTag = FareTag.NONE
    var exit = false

    fun exit(exitStation: Station) {
        exit = true
        if (mode == TransportMode.BUS)
            fareTag = FareTag.BUS
        else {
            val sortedStationsBySize = listOf(entryStation, exitStation).sortedBy { station -> station.zones.size }
            if (sortedStationsBySize[0].zones.size >= 2) {
                // only possibility EARLS_COURT
                fareTag = FareTag.ANY_ZONE_1
            } else {
                val zx = sortedStationsBySize[0].zones[0]
                val zy1 = sortedStationsBySize[1].zones[0]
                if (sortedStationsBySize[1].zones.size == 2) {
                    var tempFareTag = FareTag.MAX
                    val comparator = Comparator<FareTag> { o1, o2 -> o1.fare.compareTo(o2.fare) }
                    sortedStationsBySize[1].zones.forEach {
                        val pair = Pair(zx, it)
                        tempFareTag = if (pair.first == 1 && pair.second == 1) {
                            minOf(tempFareTag, FareTag.ANY_ZONE_1, comparator)
                        } else if (pair.first == 1 || pair.second == 1) {
                            minOf(tempFareTag, FareTag.ANY_TWO_WITH_1, comparator)
                        } else if (pair.first == pair.second) {
                            minOf(tempFareTag, FareTag.ANY_ONE_OUTSIDE_1, comparator)
                        } else {
                            minOf(tempFareTag, FareTag.ANY_TWO_WITHOUT_1, comparator)
                        }
                    }
                    fareTag = tempFareTag
                    return
                }
                when (abs(zx - zy1)) {
                    2 -> {
                        fareTag = FareTag.MORE_THAN_2
                    }
                    1 -> {
                        val pair = Pair(zx, zy1)
                        if (pair.first == 1 || pair.second == 1) {
                            fareTag = FareTag.ANY_TWO_WITH_1
                        } else fareTag = FareTag.ANY_TWO_WITHOUT_1
                    }
                    else -> {
                        fareTag = if (zx == 1)
                            FareTag.ANY_ZONE_1
                        else
                            FareTag.ANY_ONE_OUTSIDE_1
                    }
                }
            }
        }
    }
}

enum class TransportMode {
    TUBE,
    BUS,
    NONE
}

enum class FareTag(val fare: Float) {
    MAX(3.20f),
    NONE(0.00f),
    ANY_ZONE_1(2.50f),
    ANY_ONE_OUTSIDE_1(2.00f),
    ANY_TWO_WITH_1(3.00f),
    ANY_TWO_WITHOUT_1(2.25f),
    MORE_THAN_2(3.20f),
    BUS(1.80f)
}

enum class Station(val zones: List<Int>) {
    HOLBORN(listOf(1)),
    ALDGATE(listOf(1)),
    EARLS_COURT(listOf(1, 2)),
    HAMMERSMITH(listOf(2)),
    ARSENAL(listOf(2)),
    WIMBLEDON(listOf(3)),
    CHELSEA(emptyList())
}