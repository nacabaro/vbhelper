package com.github.cfogrady.vbnfc

// ConverToPages converts the byte array into the paged structure used in NFC communication
// If data for the header isn't included, the first 8 pages will be 0 filled.
fun ConvertToPages(data: ByteArray, header: ByteArray? = null) : List<ByteArray> {
    val pages = ArrayList<ByteArray>()
    // setup blank header pages
    for (i in 0..7) {
        if (header != null) {
            val index = i*4
            pages.add(header.sliceArray(index..<index+4))
        } else {
            pages.add(byteArrayOf(0, 0, 0, 0))
        }
    }
    for(i in data.indices step 4) {
        pages.add(data.sliceArray(i..<i+4))
    }
    return pages
}