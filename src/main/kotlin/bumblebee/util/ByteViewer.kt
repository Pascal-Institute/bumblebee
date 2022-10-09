package bumblebee.util

import bumblebee.util.Converter.Companion.byteToHex
import java.awt.Component
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader
import javax.swing.table.TableColumn


class ByteViewer(val byteArray : ByteArray) : JFrame() {

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

        val header = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E")

        val contents = extract()
        val table = object : JTable(contents, header){
            override fun isCellEditable(rowIndex: Int, colIndex: Int): Boolean {
                return false
            }
        }
        val rowTable =  RowNumberTable(table)

        val scrollPane = JScrollPane(table)
        scrollPane.setRowHeaderView(rowTable)

        this.add(scrollPane)
    }

    private fun extract() : Array<Array<String>>  {

        val row = byteArray.size / 16
        val col = 16

        val array = Array(row) { Array(col) { "" } }

        array.forEachIndexed { index, strings ->
            strings.forEachIndexed { idx, _ ->
                strings[idx] = byteToHex(byteArray[index * 16 + idx])
            }
        }

        return array
    }

    // Reference from http://www.camick.com/java/source/RowNumberTable.java
    class RowNumberTable(private val main: JTable) : JTable(), ChangeListener, PropertyChangeListener,
        TableModelListener {
        init {
            main.addPropertyChangeListener(this)
            main.model.addTableModelListener(this)
            isFocusable = false
            setAutoCreateColumnsFromModel(false)
            setSelectionModel(main.selectionModel)
            val column = TableColumn()
            column.headerValue = " "
            addColumn(column)
            column.cellRenderer = RowNumberRenderer()
            getColumnModel().getColumn(0).preferredWidth = 50
            preferredScrollableViewportSize = preferredSize
        }

        override fun addNotify() {
            super.addNotify()
            val c: Component = parent

            //  Keep scrolling of the row table in sync with the main table.
            if (c is JViewport) {
                val viewport: JViewport = c
                viewport.addChangeListener(this)
            }
        }

        /*
	 *  Delegate method to main table
	 */
        override fun getRowCount(): Int {
            return main.rowCount
        }

        override fun getRowHeight(row: Int): Int {
            val rowHeight = main.getRowHeight(row)
            if (rowHeight != super.getRowHeight(row)) {
                super.setRowHeight(row, rowHeight)
            }
            return rowHeight
        }

        /*
	 *  No model is being used for this table so just use the row number
	 *  as the value of the cell.
	 */
        override fun getValueAt(row: Int, column: Int): Any {
            return (row + 1).toString()
        }

        /*
	 *  Don't edit data in the main TableModel by mistake
	 */
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }

        /*
	 *  Do nothing since the table ignores the model
	 */
        override fun setValueAt(value: Any, row: Int, column: Int) {}

        //
        //  Implement the ChangeListener
        //
        override fun stateChanged(e: ChangeEvent) {
            //  Keep the scrolling of the row table in sync with main table
            val viewport: JViewport = e.source as JViewport
            val scrollPane = viewport.parent as JScrollPane
            scrollPane.verticalScrollBar.value = viewport.viewPosition.y
        }

        //
        //  Implement the PropertyChangeListener
        //
        override fun propertyChange(e: PropertyChangeEvent) {
            //  Keep the row table in sync with the main table
            if ("selectionModel" == e.propertyName) {
                setSelectionModel(main.selectionModel)
            }
            if ("rowHeight" == e.propertyName) {
                repaint()
            }
            if ("model" == e.propertyName) {
                main.model.addTableModelListener(this)
                revalidate()
            }
        }

        //
        //  Implement the TableModelListener
        //
        override fun tableChanged(e: TableModelEvent?) {
            revalidate()
        }

        /*
	 *  Attempt to mimic the table header renderer
	 */
        private class RowNumberRenderer : DefaultTableCellRenderer() {
            init {
                horizontalAlignment = JLabel.CENTER
            }

            override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): Component {
                if (table != null) {
                    val header: JTableHeader? = table.tableHeader
                    if (header != null) {
                        foreground = header.foreground
                        background = header.background
                        font = header.font
                    }
                }
                if (isSelected) {
                    font = font.deriveFont(Font.BOLD)
                }
                text = value?.toString() ?: ""
                border = UIManager.getBorder("TableHeader.cellBorder")
                return this
            }
        }
    }
}