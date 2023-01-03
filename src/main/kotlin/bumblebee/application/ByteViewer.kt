package bumblebee.application

import bumblebee.FileManager
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.intToHex
import java.awt.Component
import java.awt.Font
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.*
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader
import javax.swing.table.TableColumn


class ByteViewer(val byteArray : ByteArray) : JFrame(){

    private val HEXA = 16
    private lateinit var scrollPane : JScrollPane
    init {

        val rootPane: JRootPane = getRootPane()
        rootPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0, false), "myAction")
        var action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                val findDialog = FindDialog()
                findDialog.isVisible = true
            }
        }

        rootPane.actionMap.put("myAction", action)

        build(byteArray)
        title = "Byte Viewer"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(800, 600)
        val toolkit: Toolkit = Toolkit.getDefaultToolkit()
        val img: Image = toolkit.getImage("bumblebee_icon.png")
        iconImage = img
        setDefaultLookAndFeelDecorated(true)
    }




    private fun build(byteArray: ByteArray) {
        val menuBar = buildMenuBar()
        val header = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
        val contents = extract(byteArray)
        val table = object : JTable(contents, header){
            override fun isCellEditable(rowIndex: Int, colIndex: Int): Boolean {
                return false
            }
        }
        val rowTable =  RowNumberTable(table)
        scrollPane = JScrollPane(table)
        scrollPane.setRowHeaderView(rowTable)

        this.jMenuBar = menuBar
        this.add(scrollPane)
        isVisible = true
    }

    private fun buildMenuBar() : JMenuBar{
        val menuBar = JMenuBar()

        val findDialog = buildDialog("find")
        val contactDialog = buildDialog("contact")

        val fileMenu = JMenu("File")
        val toolMenu = JMenu("Tool")
        val aboutMenu = JMenu("About")

        val openMenuItem = JMenuItem("open")
        openMenuItem.addActionListener {
            val fileChooser = JFileChooser()
            fileChooser.showOpenDialog(this)
            this.remove(this.scrollPane)
            build(FileManager.readBytes(fileChooser.selectedFile.path))
        }

        val findMenuItem = JMenuItem("find")
        findMenuItem.addActionListener {
            findDialog.isVisible = true
        }
        val contactMenuItem = JMenuItem("contact")
        contactMenuItem.addActionListener {
            contactDialog.isVisible = true
        }

        fileMenu.add(openMenuItem)
        toolMenu.add(findMenuItem)
        aboutMenu.add(contactMenuItem)

        menuBar.add(fileMenu)
        menuBar.add(toolMenu)
        menuBar.add(aboutMenu)

        return menuBar
    }

    private fun buildDialog(title : String) : JDialog{
        val dialog = JDialog()
        dialog.title = title
        dialog.setSize(40, 20)

        return dialog
    }

    private fun extract(byteArray: ByteArray) : Array<Array<String>>  {
        val row = byteArray.size / HEXA + 1
        val col = HEXA

        val array = Array(row) { Array(col) { "" } }

        array.forEachIndexed { index, strings ->
            strings.forEachIndexed { idx, _ ->
                strings[idx] = if(index * HEXA + idx < byteArray.size) {
                    byteToHex(byteArray[index * HEXA + idx])
                }else{
                     ""
                }
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
            return intToHex(row)
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

    private class FindDialog : JDialog() {
        var findTextField = JTextField()
        init {
            add(findTextField)
        }
    }
}