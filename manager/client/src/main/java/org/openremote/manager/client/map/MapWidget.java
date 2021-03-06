/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.manager.client.map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import elemental.js.util.JsMapFromStringTo;
import elemental.json.JsonObject;
import org.openremote.manager.client.interop.mapbox.*;

import java.util.logging.Logger;

public class MapWidget extends ComplexPanel {

    private static final Logger LOG = Logger.getLogger(MapWidget.class.getName());

    protected FlowPanel host;
    protected String id;
    protected Map map;

    public MapWidget(JsonObject mapOptions) {
        this();
        initialise(mapOptions);
    }

    public MapWidget() {
        setElement(Document.get().createDivElement());
    }

    public Map getMap() {
        return map;
    }

    public boolean isInitialised() {
        return map != null;
    }

    public void initialise(JsonObject mapOptions) {
        if (map != null) {
            map.remove();
            remove(host);
        }

        if (mapOptions == null) {
            return;
        }

        id = Document.get().createUniqueId();

        host = new FlowPanel();
        host.setStyleName("flex");
        host.getElement().setId(id);
        add(host, (Element) getElement());

        mapOptions.put("container", id);
        map = new Map(mapOptions);

        map.addControl(new Navigation());

        JsMapFromStringTo<Object> popupOptions = JsMapFromStringTo.create();
        popupOptions.put("closeOnClick", false);
        new Popup(popupOptions.cast())
            .setHTML("Videolab")
            .setText("This is the Videolab.")
            .setLngLat(new LngLat(5.460315214821094, 51.44541688237109))
            .addTo(map);

        map.on(EventType.CLICK, eventData -> {
            LOG.info("### COORDS: " + eventData.getLngLat());
            LOG.info("### BOUNDS: " + map.getBounds());
        });
    }

    public void refresh() {
        // TODO: we might want to further define what "refresh" means, currently it's triggered on GoToPlaceEvent
        if (map != null) {
            map.resize();
        }
    }
}
