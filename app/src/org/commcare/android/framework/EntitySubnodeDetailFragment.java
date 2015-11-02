package org.commcare.android.framework;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.commcare.android.adapters.EntitySubnodeDetailAdapter;
import org.commcare.android.models.Entity;
import org.commcare.android.models.NodeEntityFactory;
import org.commcare.android.tasks.EntityLoaderListener;
import org.commcare.android.tasks.EntityLoaderTask;
import org.commcare.android.util.SerializationUtil;
import org.commcare.android.view.EntityView;
import org.commcare.dalvik.R;
import org.commcare.suite.model.Detail;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeReference;

import java.util.List;

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
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.modifier = savedInstanceState.getParcelable(MODIFIER_KEY);
        }

        Detail childDetail = getChildDetail();
        TreeReference childReference = this.getChildReference();

        View rootView = inflater.inflate(R.layout.entity_detail_list, container, false);
        final Activity thisActivity = getActivity();
        this.listView = ((ListView)rootView.findViewById(R.id.screen_entity_detail_list));
        if (this.adapter == null && this.loader == null && !EntityLoaderTask.attachToActivity(this)) {
            // Set up task to fetch entity data
            EntityLoaderTask theloader = new EntityLoaderTask(childDetail, this.getFactoryContext());
            theloader.attachListener(this);
            theloader.execute(childDetail.getNodeset().contextualize(childReference));

            // Add header row
            final LinearLayout headerLayout = ((LinearLayout)rootView.findViewById(R.id.entity_detail_header));
            String[] headers = new String[childDetail.getFields().length];
            for (int i = 0; i < headers.length; ++i) {
                headers[i] = childDetail.getFields()[i].getHeader().evaluate();
            }
            EntityView headerView = new EntityView(thisActivity, childDetail, headers);
            headerLayout.removeAllViews();
            headerLayout.addView(headerView);
            headerLayout.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void attach(EntityLoaderTask task) {
        this.loader = task;
    }

    @Override
    public void deliverResult(List<Entity<TreeReference>> entities, List<TreeReference> references, NodeEntityFactory factory) {
        Bundle args = getArguments();
        Detail childDetail = asw.getSession().getDetail(args.getString(DETAIL_ID));
        final int thisIndex = args.getInt(CHILD_DETAIL_INDEX, -1);
        final boolean detailCompound = thisIndex != -1;
        if (detailCompound) {
            childDetail = childDetail.getDetails()[thisIndex];
        }

        this.loader = null;
        this.adapter = new EntitySubnodeDetailAdapter(getActivity(), childDetail, references, entities, modifier);
        this.listView.setAdapter((ListAdapter)this.adapter);
    }

    @Override
    public void deliverError(Exception e) {
        ((CommCareActivity)getActivity()).displayException(e);
    }
}
