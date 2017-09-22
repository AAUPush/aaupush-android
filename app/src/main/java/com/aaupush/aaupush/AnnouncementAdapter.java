package com.aaupush.aaupush;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnViewHolder> {

    ArrayList<Announcement> announcements;

    AnnouncementAdapter(ArrayList<Announcement> announcements){
        this.announcements = announcements;
    }

    @Override
    public AnnViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.announcement_list_item, parent, false);
        return new AnnViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final AnnViewHolder holder, final int position) {
        holder.lecturerNameTv.setText(announcements.get(position).getLecturer());

        // Set the announcement to the text view if char length is less than 200
        if (announcements.get(position).getAnnouncement().length() < 200){
            holder.announcementTv.setText(announcements.get(position).getAnnouncement());
        } else { // Truncate the text and show the 'show full post' button
            final String shortAnnouncement = announcements.get(position).getAnnouncement().substring(0, 200) + "...";
            holder.fullPostShown = false;
            holder.announcementTv.setText(shortAnnouncement);
            holder.showFullPostBtn.setVisibility(View.VISIBLE);
            holder.showFullPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.fullPostShown) {
                        holder.announcementTv.setText(shortAnnouncement);
                        holder.fullPostShown = false;
                        holder.showFullPostBtn.setText(R.string.show_full_post);
                    } else {
                        holder.announcementTv.setText(announcements.get(position).getAnnouncement());
                        holder.fullPostShown = true;
                        holder.showFullPostBtn.setText(R.string.hide_full_post);
                    }
                }
            });
        }


        if (!announcements.get(position).isUrgent()){
            holder.isUrgentTv.setVisibility(View.GONE);
        } else {
            holder.isUrgentTv.setVisibility(View.VISIBLE);
        }

        String longDate = String.valueOf(announcements.get(position).getPostDate());
        Calendar postDateCalendar = PushUtils.stringToCalendar(longDate, true);

        String minuteValue = String.valueOf(postDateCalendar.get(Calendar.MINUTE)).length() == 1 ?
                "0" + postDateCalendar.get(Calendar.MINUTE) :
                "" + postDateCalendar.get(Calendar.MINUTE);
        int hourValue = postDateCalendar.get(Calendar.HOUR) == 0 ? 12 : postDateCalendar.get(Calendar.HOUR);

        String postDate = postDateCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        postDate += " " + postDateCalendar.get(Calendar.DAY_OF_MONTH);
        postDate += ", " + postDateCalendar.get(Calendar.YEAR);
        postDate += " @ " + hourValue + ":" +
                minuteValue + " " +
                postDateCalendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.ENGLISH);

        holder.datePostedTv.setText(postDate);
    }


    @Override
    public int getItemCount() {
        return announcements.size();
    }

    static class AnnViewHolder extends RecyclerView.ViewHolder{

        TextView lecturerNameTv, isUrgentTv, announcementTv, datePostedTv;
        Button showFullPostBtn;

        boolean fullPostShown = true;

        AnnViewHolder(View itemView) {
            super(itemView);

            lecturerNameTv = (TextView) itemView.findViewById(R.id.lecturer_name);
            isUrgentTv = (TextView) itemView.findViewById(R.id.is_urgent);
            announcementTv = (TextView) itemView.findViewById(R.id.announcement);
            datePostedTv = (TextView) itemView.findViewById(R.id.posted_on);
            showFullPostBtn = (Button)itemView.findViewById(R.id.show_full_post_btn);
        }
    }
}
