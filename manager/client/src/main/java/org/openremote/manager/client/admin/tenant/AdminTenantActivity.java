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
package org.openremote.manager.client.admin.tenant;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.openremote.manager.client.admin.AbstractAdminActivity;
import org.openremote.manager.client.admin.AdminView;
import org.openremote.manager.client.admin.TenantMapper;
import org.openremote.manager.client.admin.navigation.AdminNavigation;
import org.openremote.manager.client.event.bus.EventBus;
import org.openremote.manager.client.event.bus.EventRegistration;
import org.openremote.manager.client.i18n.ManagerMessages;
import org.openremote.manager.client.mvp.AppActivity;
import org.openremote.manager.client.service.RequestService;
import org.openremote.manager.client.service.SecurityService;
import org.openremote.manager.shared.Consumer;
import org.openremote.manager.shared.event.ui.ShowInfoEvent;
import org.openremote.manager.shared.security.Tenant;
import org.openremote.manager.shared.security.TenantResource;
import org.openremote.manager.shared.validation.ConstraintViolation;

import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Logger;

import static org.openremote.manager.client.http.RequestExceptionHandler.handleRequestException;

public class AdminTenantActivity
    extends AbstractAdminActivity<AdminTenantPlace, AdminTenant>
    implements AdminTenant.Presenter {

    private static final Logger LOG = Logger.getLogger(AdminTenantActivity.class.getName());

    final protected ManagerMessages managerMessages;
    final protected PlaceController placeController;
    final protected EventBus eventBus;
    final protected SecurityService securityService;
    final protected RequestService requestService;
    final protected TenantResource tenantResource;
    final protected TenantMapper tenantMapper;

    final protected Consumer<ConstraintViolation[]> validationErrorHandler = violations -> {
        for (ConstraintViolation violation : violations) {
            if (violation.getPath() != null) {
                if (violation.getPath().endsWith("displayName")) {
                    adminContent.setTenantDisplayNameError(true);
                }
                if (violation.getPath().endsWith("realm")) {
                    adminContent.setTenantRealmError(true);
                }
                if (violation.getPath().endsWith("enabled")) {
                    adminContent.setTenantEnabledError(true);
                }
            }
            adminContent.addFormMessageError(violation.getMessage());
        }
        adminContent.setFormBusy(false);
    };

    protected String realm;
    protected Tenant tenant;

    @Inject
    public AdminTenantActivity(AdminView adminView,
                               AdminNavigation.Presenter adminNavigationPresenter,
                               AdminTenant view,
                               ManagerMessages managerMessages,
                               PlaceController placeController,
                               EventBus eventBus,
                               SecurityService securityService,
                               RequestService requestService,
                               TenantResource tenantResource,
                               TenantMapper tenantMapper) {
        super(adminView, adminNavigationPresenter, view);
        this.managerMessages = managerMessages;
        this.placeController = placeController;
        this.eventBus = eventBus;
        this.securityService = securityService;
        this.requestService = requestService;
        this.tenantResource = tenantResource;
        this.tenantMapper = tenantMapper;
    }

    @Override
    protected String[] getRequiredRoles() {
        return new String[]{"read:admin", "write:admin"};
    }

    @Override
    protected AppActivity<AdminTenantPlace> init(AdminTenantPlace place) {
        realm = place.getRealm();
        return super.init(place);
    }

    @Override
    public void start(AcceptsOneWidget container, EventBus eventBus, Collection<EventRegistration> registrations) {
        super.start(container, eventBus, registrations);

        adminContent.setPresenter(this);

        adminContent.clearFormMessagesSuccess();
        adminContent.clearFormMessagesError();
        clearViewFieldErrors();
        adminContent.enableCreate(false);
        adminContent.enableUpdate(false);
        adminContent.enableDelete(false);

        if (realm != null) {
            loadTenant();
        } else {
            tenant = new Tenant();
            writeToView();
            adminContent.enableCreate(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adminContent.setPresenter(null);
        adminContent.clearFormMessagesSuccess();
        adminContent.clearFormMessagesError();
        clearViewFieldErrors();
    }

    @Override
    public void create() {
        adminContent.setFormBusy(true);
        adminContent.clearFormMessagesSuccess();
        adminContent.clearFormMessagesError();
        clearViewFieldErrors();
        readFromView();
        requestService.execute(
            tenantMapper,
            requestParams -> {
                tenantResource.create(requestParams, tenant);
            },
            204,
            () -> {
                adminContent.setFormBusy(false);
                eventBus.dispatch(new ShowInfoEvent(
                    managerMessages.tenantCreated(tenant.getDisplayName())
                ));
                placeController.goTo(new AdminTenantsPlace());
            },
            ex -> handleRequestException(ex, eventBus, managerMessages, validationErrorHandler)
        );
    }

    @Override
    public void update() {
        adminContent.setFormBusy(true);
        adminContent.clearFormMessagesSuccess();
        adminContent.clearFormMessagesError();
        clearViewFieldErrors();
        readFromView();
        requestService.execute(
            tenantMapper,
            requestParams -> {
                tenantResource.update(requestParams, realm, tenant);
            },
            204,
            () -> {
                adminContent.setFormBusy(false);
                adminContent.addFormMessageSuccess(managerMessages.tenantUpdated(tenant.getDisplayName()));
                this.realm = tenant.getRealm();
            },
            ex -> handleRequestException(ex, eventBus, managerMessages, validationErrorHandler)
        );
    }

    @Override
    public void delete() {
        adminContent.setFormBusy(true);
        adminContent.clearFormMessagesSuccess();
        adminContent.clearFormMessagesError();
        clearViewFieldErrors();
        requestService.execute(
            requestParams -> {
                tenantResource.delete(requestParams, this.realm);
            },
            204,
            () -> {
                adminContent.setFormBusy(false);
                eventBus.dispatch(new ShowInfoEvent(
                    managerMessages.tenantDeleted(tenant.getDisplayName())
                ));
                placeController.goTo(new AdminTenantsPlace());
            },
            ex -> handleRequestException(ex, eventBus, managerMessages, validationErrorHandler)
        );
    }

    @Override
    public void cancel() {
        placeController.goTo(new AdminTenantsPlace());
    }

    protected void loadTenant() {
        adminContent.setFormBusy(true);
        requestService.execute(
            tenantMapper,
            requestParams -> tenantResource.get(requestParams, realm),
            200,
            tenant -> {
                this.tenant = tenant;
                this.realm = tenant.getRealm();
                writeToView();
                adminContent.setFormBusy(false);
                adminContent.enableCreate(false);
                adminContent.enableUpdate(true);
                adminContent.enableDelete(true);
            },
            ex -> handleRequestException(ex, eventBus, managerMessages)
        );
    }

    protected void writeToView() {
        adminContent.setTenantDisplayName(tenant.getDisplayName());
        adminContent.setTenantRealm(tenant.getRealm());
        adminContent.setTenantEnabled(tenant.getEnabled());
    }

    protected void readFromView() {
        tenant.setDisplayName(adminContent.getTenantDisplayName());
        tenant.setRealm(adminContent.getTenantRealm());
        tenant.setEnabled(adminContent.getTenantEnabled());
    }

    protected void clearViewFieldErrors() {
        adminContent.setTenantDisplayNameError(false);
        adminContent.setTenantRealmError(false);
        adminContent.setTenantEnabledError(false);
    }
}
