package de.nitri.aptimob;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ScenariosActivity extends Activity {

	List<Section> sections = new ArrayList<Section>();
	List<Document> documents = new ArrayList<Document>();
	List<Resource> resources = new ArrayList<Resource>();

	private ListView lvResources;

	int currentSectionId;
	private ResourceAdapter resourceAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scenarios);

		lvResources = (ListView) findViewById(R.id.listView);

		// TODO: test data

		sections.add(new Section(1, 0, "Brand"));
		sections.add(new Section(2, 0, "Ongeval"));
		sections.add(new Section(3, 0, "Sirene"));
		sections.add(new Section(4, 1, "Taken"));

		documents.add(new Document(5, 1, "Alarmeringsprocedure", "aproc.pdf"));

		currentSectionId = getIntent().getIntExtra("section", 0);

		for (Section section : sections) {
			if (section.parentResourceId == currentSectionId)
				resources.add(section);
		}

		for (Document document : documents) {
			if (document.parentResourceId == currentSectionId)
				resources.add(document);
		}

		resourceAdapter = new ResourceAdapter(this, resources);

		lvResources.setAdapter(resourceAdapter);

		lvResources.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Resource selectedResource = resources.get(position);
				if (selectedResource.isDocument) {
					Document document = (Document) selectedResource;
					File directory = Environment.getExternalStorageDirectory();
					File file = new File(directory + "/" + document.filename);
					if (!file.exists()) {
						throw new RuntimeException("File not found");
					}
					MimeTypeMap map = MimeTypeMap.getSingleton();
					String ext = MimeTypeMap.getFileExtensionFromUrl(file
							.getName());
					String type = map.getMimeTypeFromExtension(ext);

					if (type == null)
						type = "*/*";

					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri data = Uri.fromFile(file);

					intent.setDataAndType(data, type);

					startActivity(intent);
				} else {
					Intent scenariosIntent = new Intent(getBaseContext(),
							ScenariosActivity.class);
					scenariosIntent.putExtra("section", selectedResource.id);
					startActivity(scenariosIntent);
				}
			}
		});
	}

	private class Document extends Resource {

		public String filename;

		public Document(int resourceId, int sectionResourceId, String name,
				String filename) {
			super(resourceId, sectionResourceId, name);
			isDocument = true;

		}

		@Override
		public String toString() {
			if (null != name && !name.equals("")) {
				return name;
			} else {
				return filename;
			}
		}
	}

	private class Section extends Resource {

		public String filename;

		public Section(int resourceId, int parentResourceId, String title) {
			super(resourceId, parentResourceId, title);
			isDocument = false;

		}

		@Override
		public String toString() {
			if (null != name && !name.equals("")) {
				return name;
			} else {
				return filename;
			}
		}
	}

	private class Resource {
		public int id;
		public int parentResourceId;
		public boolean isDocument;
		public String name;

		public Resource(int id, int parentResourceId, String name) {

			this.id = id;
			this.parentResourceId = parentResourceId;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private class ResourceAdapter extends ArrayAdapter<Resource> {
		private final List<Resource> resources;
		private final Context context;
		static final int rowResource = R.layout.resource_row_layout;

		public ResourceAdapter(Context context, List<Resource> resources) {
			super(context, rowResource, resources);
			this.resources = resources;
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView;
			if (convertView == null)
			rowView = inflater.inflate(rowResource, parent, false);
			else
			rowView = convertView;
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

			textView.setText((resources.get(position)).toString());

			if ((resources.get(position)).isDocument) {
				imageView.setImageResource(R.drawable.ic_document);
			} else {
				imageView.setImageResource(R.drawable.ic_folder);
			}
			return rowView;
		}

	}

	private class DownloadFile extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... sUrl) {
			try {
				URL url = new URL(sUrl[0]);
				URLConnection connection = url.openConnection();
				connection.connect();
				// this will be useful so that you can show a typical 0-100%
				// progress bar
				int fileLength = connection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(
						Environment.getExternalStorageDirectory().getPath()
								+ "file_name.extension");

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				//TODO
			}
			return null;
		}
	}
}
