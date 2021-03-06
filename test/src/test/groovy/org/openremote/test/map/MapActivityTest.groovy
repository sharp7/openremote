package org.openremote.test.map

import com.google.gwt.place.shared.PlaceController
import com.google.gwt.user.client.ui.AcceptsOneWidget
import elemental.json.JsonObject
import org.openremote.manager.client.event.bus.EventBus
import org.openremote.manager.client.i18n.ManagerMessages
import org.openremote.manager.client.interop.elemental.JsonObjectMapper
import org.openremote.manager.client.map.MapActivity
import org.openremote.manager.client.map.MapView
import org.openremote.manager.client.service.RequestServiceImpl
import org.openremote.manager.client.service.SecurityService
import org.openremote.manager.shared.Consumer
import org.openremote.manager.shared.Runnable
import org.openremote.manager.shared.http.EntityReader
import org.openremote.manager.shared.map.MapResource
import org.openremote.manager.shared.validation.ConstraintViolationReport
import org.openremote.test.ContainerTrait
import spock.lang.Specification

import static org.openremote.manager.shared.Constants.MANAGER_CLIENT_ID
import static org.openremote.manager.shared.Constants.MASTER_REALM

class MapActivityTest extends Specification implements ContainerTrait {

    def "Initialize map"() {
        given: "The fake client environment"
        def placeController = Mock(PlaceController)
        def activityContainer = Mock(AcceptsOneWidget)
        def activityBus = Mock(EventBus)
        def managerMessages = Mock(ManagerMessages)
        def activityRegistrations = []
        def constraintViolationReader = Mock(EntityReader)

        and: "the server container is started"
        def serverPort = findEphemeralPort();
        def container = startContainer(defaultConfig(serverPort), defaultServices())

        and: "An authenticated user"
        def realm = MASTER_REALM;
        def accessTokenResponse = authenticate(container, realm, MANAGER_CLIENT_ID, "test", "test")
        def securityService = Stub(SecurityService) {
            getRealm() >> realm;
            getToken() >> accessTokenResponse.getToken();
            updateToken(_, _, _) >> { int minValiditySeconds, Consumer<Boolean> successFn, Runnable errorFn ->
               successFn.accept(true);
            };
            getXsrfToken() >> "TODO: NOT ENABLED" // TODO
        }
        def requestService = new RequestServiceImpl(securityService, constraintViolationReader)

        and: "a test client target"
        def client = createClient(container).build();
        def serverUri = serverUri(serverPort);
        def clientTarget = getClientTarget(client, serverUri, realm);

        and: "The map view, resource, and activity"
        def mapView = Mock(MapView) {
            isMapInitialised() >> false
        }
        def mapResource = Stub(MapResource) {
            // This matches all methods with any parameters, of the MapResource class
            _(*_) >> { callResourceProxy(container.JSON, clientTarget, getDelegate()) }
        }

        def jsonObjectMapper = new JsonObjectMapper();

        def mapActivity = new MapActivity(
                mapView, mapResource, managerMessages, requestService, placeController, jsonObjectMapper
        )

        and: "The expected map settings"
        JsonObject mapSettings;

        when: "The activity is started"
        mapActivity.start(activityContainer, activityBus, activityRegistrations)

        then: "The view should have the activity set as presenter"
        1 * mapView.setPresenter(mapActivity)

        and: "The view should have been initialized"
        1 * mapView.initialiseMap(!null) >> { mapSettings = it[0] }

        and: "The correct map settings must be used"
        mapSettings.getArray("center").length() == 2;
        mapSettings.getArray("maxBounds").length() == 4;
        mapSettings.getNumber("maxZoom") == 18;
        mapSettings.getObject("style") != null;

        and: "the server should be stopped"
        stopContainer(container);
    }
}
