package dk.aau.cs.qweb.qboairbase.input;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import dk.aau.cs.qweb.qboairbase.types.Tuple;

public class FileStructure implements Iterator<Tuple> {
	private File file;
	private LineIterator it;
	private List<String> header;
	private int lineCount = 1;

	public FileStructure(String file) throws IOException {
		this.file = new File(file);
		it = FileUtils.lineIterator(this.file, "UTF-8");
		
		String line = it.next();
		this.header = Arrays.asList(line.split("\\s*\t\\s*"));
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Tuple next() {
		String line = it.next();
		List<String> data = new ArrayList<String>();
		for (String string : line.split("\t",-1)) {
			data.add(string);
		}
		Tuple tuple = new Tuple(data,header,lineCount);
		lineCount++;
		return tuple;
	}
	
	public int getLineCount() {
		return lineCount;
	}
}
