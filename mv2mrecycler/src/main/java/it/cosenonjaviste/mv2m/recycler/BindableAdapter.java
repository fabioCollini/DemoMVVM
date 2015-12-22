/*
 *  Copyright 2015 Fabio Collini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.cosenonjaviste.mv2m.recycler;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BindableAdapter<T> extends RecyclerView.Adapter<BindableViewHolder<T>> {

    private final ObservableList.OnListChangedCallback<ObservableList<T>> onListChangedCallback;

    private ObservableArrayList<T> items;

    private List<ViewHolderFactory<T>> viewHolderFactories = new ArrayList<>();

    private ViewHolderFactory<T> defaultViewHolderFactory;

    private List<ViewTypeSelector> viewTypeSelectors = new ArrayList<>();

    private BindListener<T> onBindListener;

    public BindableAdapter(ObservableArrayList<T> items) {
        this(items, null);
    }

    public BindableAdapter(ObservableArrayList<T> items, ViewHolderFactory<T> defaultViewHolderFactory) {
        this.items = items;
        this.defaultViewHolderFactory = defaultViewHolderFactory;
        //saved in a field to maintain a reference and avoid garbage collection
        onListChangedCallback = new AdapterOnListChangedCallback<>(this);
        items.addOnListChangedCallback(new WeakOnListChangedCallback<>(onListChangedCallback));
        if (!items.isEmpty()) {
            notifyDataSetChanged();
        }
    }

    public void addViewType(ViewHolderFactory<T> viewHolderFactory, ViewTypeSelector selector) {
        viewHolderFactories.add(viewHolderFactory);
        viewTypeSelectors.add(selector);
    }

    @Override public int getItemViewType(int position) {
        int i = 0;
        for (ViewTypeSelector selector : viewTypeSelectors) {
            if (selector.isOfViewType(position)) {
                return i;
            }
            i++;
        }
        if (defaultViewHolderFactory == null) {
            throw new RuntimeException("No factory found and no default factory available for item in position " + position);
        }
        return -1;
    }

    @Override public BindableViewHolder<T> onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ViewHolderFactory<T> factory;
        if (viewType != -1) {
            factory = viewHolderFactories.get(viewType);
        } else {
            factory = defaultViewHolderFactory;
        }
        return factory.create(viewGroup);
    }

    @Override public void onBindViewHolder(BindableViewHolder<T> viewHolder, int position) {
        viewHolder.bind(items.get(position));
        if (onBindListener != null) {
            onBindListener.call(viewHolder, position);
        }
    }

    @Override public int getItemCount() {
        return items.size();
    }

    public void setOnBindListener(BindListener<T> onBindListener) {
        this.onBindListener = onBindListener;
    }

    public interface ViewHolderFactory<T> {
        BindableViewHolder<T> create(ViewGroup viewGroup);
    }

    public interface ViewTypeSelector {
        boolean isOfViewType(int position);
    }

    public interface BindListener<T> {
        void call(BindableViewHolder<T> viewHolder, Integer position);
    }
}