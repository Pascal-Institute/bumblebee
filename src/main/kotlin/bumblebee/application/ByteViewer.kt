package bumblebee.application

import bumblebee.FileManager
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.intToHex
import java.awt.*
import java.awt.event.*
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

    companion object{
        const val HEXA = 16
        private lateinit var table: JTable

    }

    private lateinit var tableScrollPane : JScrollPane
    private lateinit var textScrollPane: JScrollPane
    private lateinit var toolBar : ToolBar
    private lateinit var statusBar : StatusBar

    init {

        this.isVisible = false

        title = "Byte Viewer"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(800, 600)
        val toolkit: Toolkit = Toolkit.getDefaultToolkit()
        val img: Image = toolkit.getImage("bumblebee_icon.png")
        iconImage = img
        setDefaultLookAndFeelDecorated(true)

        val rootPane: JRootPane = getRootPane()
        rootPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0, false), "myAction")
        val action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                val findDialog = FindDialog()
                findDialog.isVisible = true
            }
        }

        rootPane.actionMap.put("myAction", action)

        buildMenuBar()
        buildTable(byteArray)
        buildText(byteArray)
        buildStatusBar()
        buildToolBar()
        isVisible = true
    }

     fun buildStatusBar() {
        this.statusBar = StatusBar()
        this.add(statusBar, BorderLayout.PAGE_END)
    }

    private fun buildToolBar() {
        this.toolBar = ToolBar()
        this.add(toolBar, BorderLayout.PAGE_START)
    }

    private fun buildText(byteArray: ByteArray) {

        val textArea = JTextArea()

        textArea.setSize(160, 600)
        textArea.lineWrap = true
        textArea.isEditable = false
        textArea.background = background
        refine(textArea, byteArray)

        textScrollPane = JScrollPane()
        textScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        textScrollPane.setViewportView(textArea)
        add(textScrollPane, BorderLayout.EAST)
    }

    private fun refine(textArea : JTextArea, byteArray: ByteArray) {
        val text = String(byteArray)
        var str = ""

        text.forEachIndexed { index, c ->
            str += c.toString()
            if(index!= 0 && index % HEXA == 0){
                textArea.append(str)
                textArea.append("\n")
                str = ""
            }
        }
    }

    private fun buildTable(byteArray: ByteArray) {
        val header = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
        val contents = extract(byteArray)

        table = object : JTable(contents, header){
            override fun isCellEditable(rowIndex: Int, colIndex: Int): Boolean {
                return false
            }
        }

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)
                val row = table.selectedRow
                val col = table.selectedColumn
                statusBar.setLoc(row, col)
                statusBar.setValue(row, col)
            }
        })

        val rowTable =  RowNumberTable(table)

        tableScrollPane = JScrollPane(table)
        tableScrollPane.setRowHeaderView(rowTable)
        add(tableScrollPane)
    }

    private class ToolBar() : JPanel(){

        val goButton = JButton("go")
        val textField = JTextField("0")
        init {
            textField.preferredSize = Dimension(55,30)

            goButton.addMouseListener(object : MouseAdapter(){
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    val row = textField.text.toInt() / 16
                    val col = textField.text.toInt() % 16
                    table.changeSelection(row, col, false, false)
                    table.requestFocus()
                }
            }
            )

            this.add(textField)
            this.add(goButton)
            }
        }



    private class StatusBar : JPanel(){
        private val locationLabel = JLabel("Byte Viewer")
        private val valueLabel = JLabel("Value")
        init {
            add(locationLabel, BorderLayout.EAST)
            add(valueLabel)
        }
        fun setLoc(row : Int, col : Int){
            locationLabel.text = "[Loc = $row x $col : ${(row * 16 + col)}]"
        }

        fun setValue(row : Int, col : Int){
            valueLabel.text = "[Value = ${table.getValueAt(row, col).toString()}]"
        }
    }

    private fun buildMenuBar() {
        val menuBar = JMenuBar()

        val contactDialog = buildDialog("contact")

        val fileMenu = JMenu("File")
        val aboutMenu = JMenu("About")

        val openMenuItem = JMenuItem("open")
        openMenuItem.addActionListener {
            val fileChooser = JFileChooser()
            val returnVal = fileChooser.showOpenDialog(this)
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                val byteArray = FileManager.readBytes(fileChooser.selectedFile.path)

                this.isVisible = false
                this.remove(this.tableScrollPane)
                this.remove(this.textScrollPane)
                buildTable(byteArray)
                buildText(byteArray)
                this.isVisible = true
            }
        }


        val contactMenuItem = JMenuItem("contact")
        contactMenuItem.addActionListener {
            contactDialog.isVisible = true
        }

        fileMenu.add(openMenuItem)
        aboutMenu.add(contactMenuItem)

        menuBar.add(fileMenu)
        menuBar.add(aboutMenu)

        jMenuBar = menuBar
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