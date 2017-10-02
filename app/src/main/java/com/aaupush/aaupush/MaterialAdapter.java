package com.aaupush.aaupush;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class MaterialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Static variables that represent if a view is
    // a Folder, Material or a Header
    private static final int TYPE_FOLDER = 1;
    private static final int TYPE_MATERIAL = 2;
    private static final int TYPE_HEADER = 3;

    // The List
    // this will the material and course folders together
    private ArrayList<Object> list;

    // Constructor
    public MaterialAdapter(ArrayList<Object> list){
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder  viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view;

        switch (viewType){
            case TYPE_HEADER:
                view = inflater.inflate(R.layout.material_list_header, parent, false);
                viewHolder = new ListHeaderViewHolder(view);
                break;
            case TYPE_FOLDER:
                view = inflater.inflate(R.layout.material_list_item, parent, false);
                viewHolder = new FolderViewHolder(view);
                break;
            case TYPE_MATERIAL:
            default:
                view = inflater.inflate(R.layout.material_list_item, parent, false);
                viewHolder = new MaterialViewHolder(view);
                break;
        }

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER:
                configureFolder((FolderViewHolder) holder, position);
                break;
            case TYPE_HEADER:
                configureHeader((ListHeaderViewHolder)holder, position);
                break;
            case TYPE_MATERIAL:
                configureMaterial((MaterialViewHolder) holder, position);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof String){
            return TYPE_HEADER;
        } else if (list.get(position) instanceof Course){
            return TYPE_FOLDER;
        } else if (list.get(position) instanceof  Material){
            return TYPE_MATERIAL;
        }

        return -1;
    }


    private void configureHeader(ListHeaderViewHolder viewHolder, int position){
        String text = (String) list.get(position);
        viewHolder.getHeaderText().setText(text);
    }

    private void configureFolder(FolderViewHolder viewHolder, int position){
        // Get the folder from the list
        Course folder = (Course) list.get(position);

        // Hide views that are only applicable for materials
        viewHolder.getDownloadOpenBtn().setVisibility(View.GONE);
        viewHolder.getMaterialCourseName().setVisibility(View.GONE);

        // Set folder details to the views
        viewHolder.getMaterialFolderName().setText(folder.getName());
        viewHolder.getMaterialFolderDescription().setText(folder.getNumberOfFiles() + " Files");
        viewHolder.getMaterialFormat().setImageResource(R.drawable.ic_folder);
    }

    private void configureMaterial(MaterialViewHolder viewHolder, int position){
        // Get the material from the list
        final Material material = (Material) list.get(position);

        // Context
        final Context context = viewHolder.downloadOpenBtn.getContext().getApplicationContext();

        // Set material details to the views
        viewHolder.getMaterialFolderName().setText(material.getTitle());
        viewHolder.getMaterialFolderDescription().setText(material.getDescription());

        // Get Course Name
        final DBHelper dbHelper = new DBHelper(context);
        String courseName = dbHelper.getCourse(material.getParentCourseId()).getName();

        // Get file Size
        String fileSize = material.getFileSize() + " KB";

        // Get Published Date
        Calendar publishedDateCal = PushUtils.stringToCalendar(String.valueOf(material.getPublishedDate()), false);
        String publishedDateText = publishedDateCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        publishedDateText += " " + publishedDateCal.get(Calendar.DAY_OF_MONTH);

        // Append Material Detail Text(eg 'Programming | Jun 20 | 500 KB')
        String detailText = courseName + " | " + publishedDateText + " | " + fileSize;

        // Set the detail text to the text view
        viewHolder.getMaterialDetail().setText(detailText);


        // Set download, open or cancel button for downloads
        switch (material.getAvailableOfflineStatus()){
            case Material.MATERIAL_AVAILABLE_OFFLINE:
                viewHolder.getDownloadOpenBtn().setImageDrawable(
                        context.getResources().getDrawable(R.drawable.ic_open_in_new)
                );
                break;
            case Material.MATERIAL_NOT_AVAILABLE:
                viewHolder.getDownloadOpenBtn().setImageDrawable(
                        context.getResources().getDrawable(R.drawable.ic_file_download)
                );
                break;
            case Material.MATERIAL_DOWNLOADING:
                viewHolder.getDownloadOpenBtn().setImageDrawable(
                        context.getResources().getDrawable(R.drawable.ic_cancel)
                );
                viewHolder.getDownloadProgressBar().setVisibility(View.VISIBLE);
                break;
        }

        // Set file format image
        switch (material.getFileFormat().toUpperCase()){
            case "PDF":
                viewHolder.getMaterialFormat().setImageResource(R.drawable.ic_pdf);
                break;
            case "DOCX":
            case "DOC":
            case "WORD":
                viewHolder.getMaterialFormat().setImageResource(R.drawable.ic_word);
                break;
            case "PPTX":
            case "POWER_POINT":
                viewHolder.getMaterialFormat().setImageResource(R.drawable.ic_power_point);
                break;
            default:
                // TODO: Add more icons by release

                // File type is unknown, so build a text drawable based on the format
                TextDrawable fileDrawable = TextDrawable.builder()
                        .beginConfig().fontSize(PushUtils.convertSpToPixels(18, context)).endConfig()
                        .buildRound(material.getFileFormat(), Color.GRAY);
                // set the drawable
                viewHolder.getMaterialFormat().setImageDrawable(fileDrawable);
                break;
        }

        // Set on click listener for the  download, open or cancel button
        viewHolder.getDownloadOpenBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: start download, open file or cancel download
                if (material.getAvailableOfflineStatus() == Material.MATERIAL_NOT_AVAILABLE) {
                    Toast.makeText(context, "Starting Download", Toast.LENGTH_SHORT).show();
                    PushService.downloadMaterial(material, context);
                }
                else if (material.getAvailableOfflineStatus() == Material.MATERIAL_DOWNLOADING) {
                    DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                    downloadManager.remove(material.getDownloadID());
                }
                else if (material.getAvailableOfflineStatus() == Material.MATERIAL_AVAILABLE_OFFLINE) {
                    Intent openFile = new Intent(Intent.ACTION_VIEW);
                    MimeTypeMap typeMap = MimeTypeMap.getSingleton();
                    String type = typeMap.getMimeTypeFromExtension(material.getFileFormat().toLowerCase());

                    if (type == null) {
                        type = "*/*";
                    }

                    Uri uri = Uri.parse(material.getOfflineLocation());

                    openFile.setDataAndType(uri, type);
                    try {
                        context.getApplicationContext().startActivity(openFile);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context,
                                "No application found to open the file! File Format: " + material.getFileFormat(),
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        // Close DBObject
        dbHelper.close();
    }

    void addHeader(int position, String header) {
        list.add(position, header);
    }


    class ListHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView headerText;

        ListHeaderViewHolder(View itemView) {
            super(itemView);
            headerText = (TextView) itemView.findViewById(R.id.header_text);
        }

        TextView getHeaderText() {
            return headerText;
        }
    }

    class MaterialViewHolder extends RecyclerView.ViewHolder {

        private ImageView materialFormat;
        private TextView materialFolderName;
        private TextView materialFolderDescription;
        private TextView materialDetail;
        private ImageButton downloadOpenBtn;
        private ProgressBar downloadProgressBar;

        MaterialViewHolder(View itemView) {
            super(itemView);

            materialFormat = (ImageView) itemView.findViewById(R.id.file_type_img);
            materialFolderName = (TextView) itemView.findViewById(R.id.file_name);
            materialFolderDescription = (TextView) itemView.findViewById(R.id.file_description);
            materialDetail = (TextView) itemView.findViewById(R.id.course_name);
            downloadOpenBtn = (ImageButton) itemView.findViewById(R.id.download_open_btn);
            downloadProgressBar = (ProgressBar) itemView.findViewById(R.id.download_progress_bar);
        }

        ImageView getMaterialFormat() {
            return materialFormat;
        }

        TextView getMaterialFolderName() {
            return materialFolderName;
        }

        TextView getMaterialFolderDescription() {
            return materialFolderDescription;
        }

        TextView getMaterialDetail() {
            return materialDetail;
        }

        ImageButton getDownloadOpenBtn() {
            return downloadOpenBtn;
        }

         ProgressBar getDownloadProgressBar() {
            return downloadProgressBar;
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView materialFormat;
        private TextView materialFolderName;
        private TextView materialFolderDescription;
        private TextView materialCourseName;
        private ImageButton downloadOpenBtn;

        FolderViewHolder(View itemView) {
            super(itemView);

            materialFormat = (ImageView) itemView.findViewById(R.id.file_type_img);
            materialFolderName = (TextView) itemView.findViewById(R.id.file_name);
            materialFolderDescription = (TextView) itemView.findViewById(R.id.file_description);
            materialCourseName = (TextView) itemView.findViewById(R.id.course_name);
            downloadOpenBtn = (ImageButton) itemView.findViewById(R.id.download_open_btn);

            // Set on click listener for clicks on folders
            itemView.setOnClickListener(this);
        }

        ImageView getMaterialFormat() {
            return materialFormat;
        }

        TextView getMaterialFolderName() {
            return materialFolderName;
        }

        TextView getMaterialFolderDescription() {
            return materialFolderDescription;
        }

        TextView getMaterialCourseName() {
            return materialCourseName;
        }

        ImageButton getDownloadOpenBtn() {
            return downloadOpenBtn;
        }


        @Override
        public void onClick(View view) {
            // Get clicked folder id
            Course folder = (Course)list.get(getAdapterPosition());
            int folderId = folder.getCourseID();

            // Send broadcast to notify fragment about click on a folder
            Intent intent = new Intent(PushUtils.ON_FOLDER_CLICK_BROADCAST);
            intent.putExtra(MaterialFragment.INTENT_EXTRA_COURSE_ID, folderId);
            view.getContext().getApplicationContext().
                    sendBroadcast(intent);
        }
    }
}
