package com.jdamcd.buildmonkey;

import java.util.List;

import com.jdamcd.buildmonkey.pojo.Job;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class JobAdapter extends ArrayAdapter<Job> {
	
	private Activity context;
	private List<Job> projects;
	
	public JobAdapter(Activity context, List<Job> projects) {
		super(context,  R.layout.item_job_grid, projects);
		this.context = context;
		this.projects = projects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View project = convertView;
		ProjectHolder holder = null;
		
		if (project == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			project = inflater.inflate(R.layout.item_job_grid, parent, false);
			holder = new ProjectHolder(project);
			project.setTag(holder);
		} else {
			holder = (ProjectHolder) project.getTag();
		}
		
		Job data = projects.get(position);
		holder.getName().setText(data.getName());
		setStatusColour(holder, data.getColor());
		return project;
	}

    private void setStatusColour(ProjectHolder holder, String status) {
        Resources res = context.getResources();
        if (status.equals("blue")) {
		    holder.getStatus().setBackgroundColor(res.getColor(R.color.status_green));
		} else if (status.equals("red")) {
		    holder.getStatus().setBackgroundColor(res.getColor(R.color.status_red));
		} else if (status.equals("aborted")) {
		    holder.getStatus().setBackgroundColor(res.getColor(R.color.status_grey));
		} else {
		    holder.getStatus().setBackgroundColor(res.getColor(R.color.status_yellow));
		}
    }
	
	private class ProjectHolder {
		
		View base;
		TextView name = null;
		View status = null;
		
		ProjectHolder(View base) {
			this.base = base;
		}
		
		TextView getName() {
			if (name == null) {
				name = (TextView) base.findViewById(R.id.project_title);
			}
			return name;
		}
		
		View getStatus() {
		    if (status == null) {
		        status = base.findViewById(R.id.build_status);
		    }
		    return status;
		}
		
	}

}
