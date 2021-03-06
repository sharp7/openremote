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
package org.openremote.manager.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import org.openremote.manager.client.style.ThemeStyle;
import org.openremote.manager.client.admin.AdminPlace;
import org.openremote.manager.client.admin.overview.AdminOverviewPlace;
import org.openremote.manager.client.assets.AssetsPlace;
import org.openremote.manager.client.flows.FlowsPlace;
import org.openremote.manager.client.map.MapPlace;
import org.openremote.manager.client.user.UserAccountPlace;
import org.openremote.manager.client.widget.PushButton;

public class HeaderViewImpl extends Composite implements HeaderView {

    interface UI extends UiBinder<HTMLPanel, HeaderViewImpl> {
    }

    private static UI ui = GWT.create(UI.class);
    private Presenter presenter;

    @UiField
    ThemeStyle themeStyle;

    @UiField
    HTMLPanel quickAccess;

    @UiField
    PushButton mapButton;

    @UiField
    PushButton assetsButton;

    @UiField
    PushButton rulesButton;

    @UiField
    PushButton flowsButton;

    @UiField
    PushButton adminButton;

    @UiField
    PushButton userButton;

    @Inject
    public HeaderViewImpl() {
        initWidget(ui.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

        mapButton.setEnabled(presenter.isUserInRole("read:map"));
        assetsButton.setEnabled(presenter.isUserInRole("read:assets"));
        rulesButton.setEnabled(presenter.isUserInRole("read:rules"));
        flowsButton.setEnabled(presenter.isUserInRole("read:flows"));
        adminButton.setEnabled(presenter.isUserInRole("read:admin"));
    }

    @Override
    public void onPlaceChange(Place place) {
        mapButton.removeStyleName(themeStyle.NavItemActive());
        assetsButton.removeStyleName(themeStyle.NavItemActive());
        flowsButton.removeStyleName(themeStyle.NavItemActive());
        adminButton.removeStyleName(themeStyle.NavItemActive());
        userButton.removeStyleName(themeStyle.NavItemActive());

        if (place instanceof MapPlace) {
            mapButton.addStyleName(themeStyle.NavItemActive());
        }
        if (place instanceof AssetsPlace) {
            assetsButton.addStyleName(themeStyle.NavItemActive());
        }
        if (place instanceof FlowsPlace) {
            flowsButton.addStyleName(themeStyle.NavItemActive());
        }
        if (place instanceof AdminPlace) {
            adminButton.addStyleName(themeStyle.NavItemActive());
        }
        if (place instanceof UserAccountPlace) {
            userButton.addStyleName(themeStyle.NavItemActive());
        }
    }

    @Override
    public void setUsername(String username) {
        userButton.setText(username != null ? username : "");
        userButton.setEnabled(username != null && username.length() > 0);
    }

    @UiHandler("mapButton")
    void mapClicked(ClickEvent e) {
        presenter.goTo(new MapPlace());
    }

    @UiHandler("assetsButton")
    void assetsClicked(ClickEvent e) {
        presenter.goTo(new AssetsPlace());
    }

    @UiHandler("adminButton")
    void adminClicked(ClickEvent e) {
        presenter.goTo(new AdminOverviewPlace());
    }

    @UiHandler("flowsButton")
    void flowsClicked(ClickEvent e) {
        presenter.goTo(new FlowsPlace());
    }

    @UiHandler("userButton")
    public void userClicked(final ClickEvent event) {
        presenter.getUserControls().toggleRelativeTo(userButton);
    }
}