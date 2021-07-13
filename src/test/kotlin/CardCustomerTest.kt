import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CardCustomerTest {
    lateinit var customer: Customer

    @Before
    fun setUp() {
        customer = Customer()
        customer.loadCard(30.0f)
    }

    @Test
    fun `All finished trips results in correct fare calculation`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.HOLBORN)
            startTrip(trip1)
            endTrip(trip1, Station.EARLS_COURT)

            val trip2 = Trip(TransportMode.BUS, Station.EARLS_COURT)
            startTrip(trip2)
            endTrip(trip2, Station.CHELSEA)

            val trip3 = Trip(TransportMode.TUBE, Station.EARLS_COURT)
            startTrip(trip3)
            endTrip(trip3, Station.HAMMERSMITH)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(23.7f)
    }

    @Test
    fun `Any tube unfinished trip results in full fare deduction`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.HOLBORN)
            startTrip(trip1)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(26.8f)
    }

    @Test
    fun `Any bus unfinished trip results in full fare deduction`() {
        customer.run {
            val trip1 = Trip(TransportMode.BUS, Station.HOLBORN)
            startTrip(trip1)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(28.2f)
    }

    @Test
    fun `Verify fare calculation for anywhere in zone 1`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.HOLBORN)
            startTrip(trip1)
            endTrip(trip1, Station.EARLS_COURT)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(30.0f - FareTag.ANY_ZONE_1.fare)
    }

    @Test
    fun `Verify fare calculation for any zone outside zone 1`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.HAMMERSMITH)
            startTrip(trip1)
            endTrip(trip1, Station.ARSENAL)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(30.0f - FareTag.ANY_ONE_OUTSIDE_1.fare)
    }

    @Test
    fun `Verify fare calculation for any two zones including zone 1`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.HAMMERSMITH)
            startTrip(trip1)
            endTrip(trip1, Station.HOLBORN)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(30.0f - FareTag.ANY_TWO_WITH_1.fare)
    }

    @Test
    fun `Verify fare calculation for any two zones excluding zone 1`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.ARSENAL)
            startTrip(trip1)
            endTrip(trip1, Station.WIMBLEDON)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(30.0f - FareTag.ANY_TWO_WITHOUT_1.fare)
    }

    @Test
    fun `Verify fare calculation for more than 2 zones`() {
        customer.run {
            val trip1 = Trip(TransportMode.TUBE, Station.WIMBLEDON)
            startTrip(trip1)
            endTrip(trip1, Station.ALDGATE)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(30.0f - FareTag.MORE_THAN_2.fare)
    }

    @Test
    fun `Verify fare calculation for any bus journey`() {
        customer.run {
            val trip1 = Trip(TransportMode.BUS, Station.ARSENAL)
            startTrip(trip1)
            endTrip(trip1, Station.CHELSEA)
        }
        assertThat(customer.checkCardBalance()).isEqualTo(30.0f - FareTag.BUS.fare)
    }
}