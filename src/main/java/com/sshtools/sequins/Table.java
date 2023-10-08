/**
 * Copyright © 2023 JAdaptive Limited (support@jadaptive.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.sequins;

import static com.sshtools.sequins.Sequence.repeat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.sequins.Sequence.BoxChar;
import com.sshtools.sequins.Twidget.AbstractTwidget;

public class Table extends AbstractTwidget {

	public enum Alignment {
		CENTER, LEFT, RIGHT
	}
	
	public enum ResizeMode {
		FULL_WIDTH, LAST_TAKES_SPACE, FIRST_TAKES_SPACE, COMPACT
	}

	private boolean border = true;
	private Row footer = null;
	private Row header = null;
	private int insets = 1;
	private int maxCellWidth = -1;
	private int minCellWidth = -1;
	private List<Row> rows = new ArrayList<Row>();
	private ResizeMode resizeMode = ResizeMode.FULL_WIDTH;
	private CellRenderer<Object> cellRenderer;
	private Map<Class<?>, CellRenderer<?>> renderers =new HashMap<>();

	public Table(Sequins terminal, String... headers) {
		super(terminal);
		cellRenderer = CellRenderer.defaultRenderer(terminal);
		
		if(headers.length > 0)
			header(new Row(headers).strong(true));
	}

	public CellRenderer<?> defaultCellRenderer() {
		return cellRenderer;
	}

	public <R> Table cellRenderer(Class<R> clazz, CellRenderer<R> cellRenderer) {
		renderers.put(clazz, cellRenderer);
		return this;
	}

	public Table defaultCellRenderer(CellRenderer<Object> cellRenderer) {
		this.cellRenderer = cellRenderer;
		return this;
	}

	public ResizeMode resizeMode() {
		return resizeMode;
	}
	
	public Table resizeMode(ResizeMode resizeMode) {
		this.resizeMode = resizeMode;
		return this;
	}
	
	public boolean bordered() {
		return border;
	}
	
	public void bordered(boolean border) {
		this.border = border;
	}
	
	public int insets() {
		return insets;
	}
	
	public void insets(int insets) {
		this.insets = insets;
	}

	@Override
	public Sequence draw(DrawContext context, Sequence seq) throws IOException {
		
		var maxColumnWidths = new ArrayList<Integer>();
		if (header != null) {
			checkRow(context, -1, maxColumnWidths, header);
		}
		for (int i = 0; i < rows.size(); i++) {
			checkRow(context, i, maxColumnWidths, rows.get(i));
		}
		if (footer != null) {
			checkRow(context, -1, maxColumnWidths, footer);
		}

		var top = new StringBuilder(drawBoxChar(BoxChar.BOX_TOP_LEFT));
		var bottom = new StringBuilder(drawBoxChar(BoxChar.BOX_BOTTOM_LEFT));
		var middle = new StringBuilder(drawBoxChar(BoxChar.BOX_MIDDLE_LEFT));
		boolean first = true;
		for (var i : maxColumnWidths) {
			if (!first) {
				middle.append(drawBoxChar(BoxChar.BOX_MIDDLE_MIDDLE));
			}
			if (!first) { 
				top.append(drawBoxChar(BoxChar.BOX_TOP_MIDDLE));
			}
			if (!first) {
				bottom.append(drawBoxChar(BoxChar.BOX_BOTTOM_MIDDLE));
			}
			first = false;
			middle.append(repeat(drawBoxChar(BoxChar.BOX_MIDDLE), i + (insets * 2)));
			top.append(repeat(drawBoxChar(BoxChar.BOX_TOP), i + (insets * 2)));
			bottom.append(repeat(drawBoxChar(BoxChar.BOX_BOTTOM), i + (insets * 2)));
		}
		middle.append(drawBoxChar(BoxChar.BOX_MIDDLE_RIGHT));
		top.append(drawBoxChar(BoxChar.BOX_TOP_RIGHT));
		bottom.append(drawBoxChar(BoxChar.BOX_BOTTOM_RIGHT));

		if(bordered()) {
			seq.str(top.toString());
			seq.lf();
		}
		if (header != null) {
			printRow(seq, header, maxColumnWidths);
			if(bordered()) {
				seq.str(middle.toString());
				seq.lf();
			}
		}
		for (Row row : rows) {
			printRow(seq, row, maxColumnWidths);
		}
		if (footer != null) {
			seq.str(middle.toString());
			seq.lf();
			printRow(seq, footer, maxColumnWidths);
		}
		
		if(bordered()) {
			seq.str(bottom.toString());
			seq.lf();
		}

		return seq;
	}

	public Row footer() {
		return footer;
	}

	public Table footer(Row footer) {
		this.footer = footer;
		return this;
	}

	public Row header() {
		return header;
	}

	public Table header(Row header) {
		this.header = header;
		return this;
	}

	public int maxCellWidth() {
		return maxCellWidth;
	}
	
	public Table maxCellWidth(int maxCellWidth) {
		this.maxCellWidth = maxCellWidth;
		return this;
	}
	
	public int minCellWidth() {
		return minCellWidth;
	}
	
	public Table minCellWidth(int minCellWidth) {
		this.minCellWidth = minCellWidth;
		return this;
	}
	
	public Table row(Row row) {
		rows.add(row);
		return this;
	}

	public List<Row> rows() {
		return Collections.unmodifiableList(rows);
	}

	protected int checkCell(int maxw, int rocwIndex, Cell<?> cell) {
		var seq = getRenderer(cell).render(cell, maxw);
		var size = seq.textLength();
		if(cell.minWidth() != -1 && size < cell.minWidth()) {
			size = cell.minWidth();
		}
		if(cell.maxWidth() != -1 && size > cell.maxWidth()) {
			size = cell.maxWidth();
		}
		size += insets * 2;
		if(size > maxw) 
			size = maxw;
		return size;
	}

	@SuppressWarnings("unchecked")
	private CellRenderer<Object> getRenderer(Cell<?> cell) {
		var r = renderers.get(cell.value() == null ? null : cell.value().getClass());
		return r == null ? cellRenderer : (CellRenderer<Object>) r;
	}

	protected String formatRow(int rowLine, Row row, List<Integer> maxColumnWidths) {
		Iterator<Integer> width = maxColumnWidths.iterator();
		var buf = getTerminal().createSequence();
		int empty = 0;
		int col = 0;
		for (Cell<?> cell : row) {
			int maxw = width.next();
			var val = renderCell(rowLine, cell, maxw, col);
			if (val.emptyText()) {
				empty++;
			}
			var align = cell.alignment() == null ? row.defaultAlignment() : cell.alignment();
//			System.err.println("for: " + val.toString() + " maxw: " + maxw + "and textlen: " + val.textLength());
			
			
			switch(align) {
			case RIGHT:
				buf.ch(maxw - val.textLength(), ' ');
				break;
			case CENTER:
				buf.ch((maxw - val.textLength() )/ 2, ' ');
				break;
			default:
				break;
			}
			
			buf.ch(insets, ' ');
			if(cell.strong() || row.strong()) {
				buf.boldOn();
			}
			buf.seq(val);
			if(cell.strong() || row.strong()) {
				buf.boldOff();
			}
			buf.ch(insets, ' ');
			
			switch(align) {
			case LEFT:
				buf.ch(maxw - val.textLength(), ' ');
				break;
			case CENTER:
				buf.ch(maxw - ( (maxw - val.textLength() )/ 2 ), ' ');
				break;
			default:
				break;
			}
			
			if (width.hasNext() && bordered()) {
				buf.str(drawBoxChar(BoxChar.BOX_CENTER));
			}
			
			col++;
		}
		if (empty == row.size()) {
			return null;
		}
		return buf.toString();
	}

	protected Sequence renderCell(int rowLine, Cell<?> cell, int maxw, int col) {
//		System.out.println("render " + col + ", " + rowLine + " cell: " + cell + " in " + maxw);
		// TODO support multiline rows again
		if(rowLine > 0) {
			return Sequence.empty();
		}
		return getRenderer(cell).render(cell, maxw);
	}

	private void checkRow(DrawContext draw, int rowIndex, List<Integer> maxColumWidths, Row row) {
		while (maxColumWidths.size() < row.size()) {
			maxColumWidths.add(0);
		}
		
		var w = draw.getWidth();
		var available = w - ( bordered() ? 1 + maxColumWidths.size() : 0 ) - ( ( insets * 2 ) * maxColumWidths.size() );
		var pc = available / row.size();
		
		switch(resizeMode) {
		case FULL_WIDTH:

			for (var i = 0; i < maxColumWidths.size(); i++) {
				maxColumWidths.set(i, Math.max(pc, maxColumWidths.get(i)));
			}
			break;
		case COMPACT:
			for (var i = maxColumWidths.size() - 1; i >= 0; i--) {
				int cellWidth = checkCell(pc, rowIndex, row.get(i));
				maxColumWidths.set(i, Math.max(cellWidth, maxColumWidths.get(i)));
			}
			break;
		default:
			throw new UnsupportedOperationException("TODO");
		}
		
	}

	private String drawBoxChar(BoxChar boxChar) {
		var bui = getTerminal().createSequence();
		bui.grOn();
		bui.box(boxChar);
		bui.grOff();
		return bui.toString();
	}

	private void printRow(Sequence seq, Row row, List<Integer> maxColumnWidths) throws IOException {
		int rowLine = 0;
		while (true) {
			var formatRow = formatRow(rowLine, row, maxColumnWidths);
			if (formatRow == null) {
				break;
			}
			if(bordered())
				seq.box(BoxChar.BOX_LEFT);
			
			seq.str(formatRow);
			
			if(bordered())
				seq.box(BoxChar.BOX_RIGHT);
			
			seq.lf();
			rowLine++;
		}
	}
}
