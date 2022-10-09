package bumblebee.util

import bumblebee.util.Converter.Companion.byteToHex
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.WindowConstants


class ByteViewer(val byteArray : ByteArray) : JFrame() {

    lateinit var table: JTable

    init {
        build()
        title = "Byte Viewer"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(800, 600)
        setDefaultLookAndFeelDecorated(true)
        isVisible = true
    }

    private fun build() {
        extract()
        table = JTable()

        val header = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E")
        val contents = extract()
        val table = JTable(contents, header)
        val scrollPane = JScrollPane(table)

        this.add(scrollPane)
    }

    private fun extract() : Array<Array<String>>  {

        val row = byteArray.size / 16
        val col = 16

        var array = Array(row) { Array(col) { "" } }

        array.forEachIndexed { index, strings ->
            strings.forEachIndexed { idx, s ->
                strings[idx] = byteToHex(byteArray[index * 16 + idx])
            }
        }

        return array

    }
}