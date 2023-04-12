package idv.teddy

import com.github.javafaker.Faker
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.absoluteValue


class InvoiceTest {


    private val unitPrice = BigDecimal("19.99")
    private val percentDiscount = BigDecimal("30")
    private lateinit var testObjects: MutableList<Any>
    private lateinit var product: Product

    @BeforeEach
    fun setUp() {
        testObjects = ArrayList()
        product = createProduct(unitPrice)
    }

    @AfterEach
    fun tearDown() {
        val i = testObjects.iterator()
        while (i.hasNext()) {
            try {
                deleteObject(i.next())
            } catch (e: Exception) {
                // Nothing to do; we just want to make sure we continue on to the next object in the list
            }
        }
    }

    @Test
    fun testAddItemQuantity_severalQuantity_v1() {
        // setup fixture
        val several = 5
        val invoice = createCustomerInvoice(percentDiscount)

        // Exercise SUT
        invoice.addItemQuantity(product, several)

        // Verify outcome
        val basePrice = unitPrice.multiply(BigDecimal.valueOf(several.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = createLineItem(invoice, several, extendedPrice)
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    private fun createCustomerInvoice(percentDiscount: BigDecimal) = createInvoice(createACustomer(percentDiscount))

    @Test
    fun testAddItemQuantity_oneQuantity() {
        // setup fixture
        val quantityOne = 1
        val invoice = createCustomerInvoice(percentDiscount)

        // Exercise SUT
        invoice.addItemQuantity(product, quantityOne)

        // Verify outcome
        val basePrice = unitPrice.multiply(BigDecimal.valueOf(quantityOne.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = createLineItem(invoice, quantityOne, extendedPrice)
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    @Test
    fun testChangeQuantity_severalQuantity() {
        // setup fixture
        val several = 3
        val newQuantity = several + 2
        val invoice = createCustomerInvoice(percentDiscount)
        invoice.addItemQuantity(product, several)

        // Exercise SUT
        invoice.changeQuantityForProduct(product, newQuantity)

        // Verify outcome
        val basePrice = unitPrice.multiply(BigDecimal.valueOf(newQuantity.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = createLineItem(invoice, newQuantity, extendedPrice)
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    private fun createLineItem(invoice: Invoice, quantity: Int, extendedPrice: BigDecimal): LineItem {
        val expected = LineItem(invoice, product, quantity)
        mockkObject(expected)
        every { expected.getPercentDiscount() } returns percentDiscount
        every { expected.getExtendedPrice() } returns extendedPrice
        return expected
    }

    private fun createACustomer(percentDiscount: BigDecimal): Customer {
        val billingAddress = createAddress()
        val shippingAddress = createAddress()
        val customer = Customer(getUniqueNumber(), "John", "Doe", percentDiscount, billingAddress, shippingAddress)
        registerTestObjects(customer)
        return customer
    }

    private fun createInvoice(customer: Customer): Invoice {
        val invoice = Invoice(customer)
        registerTestObjects(invoice)
        return invoice
    }

    private fun createProduct(unitPrice: BigDecimal): Product {
        val product = Product(getUniqueNumber(), "SomeWidget", unitPrice)
        registerTestObjects(product)
        return product
    }

    private fun getUniqueNumber() = UUID.randomUUID().hashCode().absoluteValue
    private fun createAddress(): Address {
        val faker = Faker()
        val addressFaker = faker.address()
        val address = Address(addressFaker.fullAddress(), addressFaker.city(), addressFaker.state(), addressFaker.zipCode(), addressFaker.country())
        registerTestObjects(address)
        return address
    }

    private fun assertContainsExactlyOneLineItem(invoice: Invoice, expected: LineItem) {
        val lineItems: List<LineItem> = invoice.getLineItems()
        assertEquals(1, lineItems.size)
        val actItem: LineItem = lineItems[0]
        assertEquals(expected.getInv(), actItem.getInv(), "invoice")
        assertEquals(expected.getProd(), actItem.getProd(), "product")
        assertEquals(expected.getQuantity(), actItem.getQuantity(), "quantity")
        assertEquals(expected.getPercentDiscount(), actItem.getPercentDiscount(), "discount")
        assertEquals(expected.getUnitPrice(), actItem.getUnitPrice(), "unit price")
        assertEquals(expected.getExtendedPrice(), actItem.getExtendedPrice(), "extended price")
    }

    private fun deleteObject(@Suppress("UNUSED_PARAMETER") obj: Any?) {
    }

    private fun registerTestObjects(testObject: Any) {
        testObjects.add(testObject)
    }

}
