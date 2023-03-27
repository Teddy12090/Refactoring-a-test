package idv.teddy

data class Invoice(val customer: Customer) {
    private val lineItems = ArrayList<LineItem>()

    fun addItemQuantity(product: Product, quantity: Int) {
        lineItems.add(LineItem(this, product, quantity))
    }

    fun getLineItems(): List<LineItem> {
        return lineItems
    }

}
