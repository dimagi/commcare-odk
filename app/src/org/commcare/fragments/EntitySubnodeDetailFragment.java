package org.commcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.commcare.activities.CommCareActivity;
import org.commcare.adapters.EntitySubnodeDetailAdapter;
import org.commcare.cases.entity.Entity;
import org.commcare.cases.entity.NodeEntityFactory;
import org.commcare.dalvik.R;
import org.commcare.suite.model.Detail;
import org.commcare.tasks.EntityLoaderListener;
import org.commcare.tasks.EntityLoaderTask;
import org.commcare.views.EntityView;
import org.javarosa.core.model.instance.TreeReference;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by jschweers on 8/26/2015.
 * <p/>
 * An EntityDetailSubnodeFragment displays a detail with a row for each of a set of
 * nodes generated by expanding a given nodeset expression in the context of a given entity.
 */
public class EntitySubnodeDetailFragment extends EntityDetailFragment implements EntityLoaderListener {
    private EntityLoaderTask loader;
    private ListView listView;

    public EntitySubnodeDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.modifier = savedInstanceState.getParcelable(MODIFIER_KEY);
        }

        Detail detailToDisplay = getDetailToUseForDisplay();
        TreeReference referenceToDisplay = getReferenceToDisplay();

        View rootView = inflater.inflate(R.layout.entity_detail_list, container, false);

        final AppCompatActivity thisActivity = (AppCompatActivity)getActivity();
        this.listView = rootView.findViewById(R.id.screen_entity_detail_list);

        if (this.adapter == null && this.loader == null && !EntityLoaderTask.attachToActivity(this)) {
            // Set up task to fetch entity data
            EntityLoaderTask theLoader =
                    new EntityLoaderTask(detailToDisplay, getFactoryContextForRef(referenceToDisplay));
            theLoader.attachListener(this);
            theLoader.executeParallel(detailToDisplay.getNodeset().contextualize(referenceToDisplay));

            // Add header row
            final LinearLayout headerLayout = rootView.findViewById(R.id.entity_detail_header);
            String[] headers = new String[detailToDisplay.getFields().length];
            for (int i = 0; i < headers.length; ++i) {
                headers[i] = detailToDisplay.getFields()[i].getHeader().evaluate();
            }
            EntityView headerView = EntityView.buildHeadersEntityView(thisActivity, detailToDisplay,
                    headers, false);
            headerLayout.removeAllViews();
            headerLayout.addView(headerView);
            headerLayout.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void attachLoader(EntityLoaderTask task) {
        this.loader = task;
    }

    @Override
    public void deliverLoadResult(List<Entity<TreeReference>> entities,
                                  List<TreeReference> references,
                                  NodeEntityFactory factory, int focusTargetIndex) {
        Bundle args = getArguments();
        Detail detail = asw.getSession().getDetail(args.getString(DETAIL_ID));
        final int thisIndex = args.getInt(CHILD_DETAIL_INDEX, -1);
        final boolean detailCompound = thisIndex != -1;
        if (detailCompound) {
            detail = getChildDetailForDisplay(detail, thisIndex);
        }

        this.loader = null;
        this.adapter = new EntitySubnodeDetailAdapter(getActivity(), detail, references,
                entities, modifier, factory);
        this.listView.setAdapter((ListAdapter)this.adapter);
        if (focusTargetIndex != -1) {
            listView.setSelection(focusTargetIndex);
        }
    }

    @Override
    public void deliverLoadError(Exception e) {
        ((CommCareActivity)getActivity()).displayCaseListLoadException(e);
    }
}
