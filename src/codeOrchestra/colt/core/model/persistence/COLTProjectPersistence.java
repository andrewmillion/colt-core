package codeOrchestra.colt.core.model.persistence;

import codeOrchestra.colt.core.loading.LiveCodingHandlerManager;
import codeOrchestra.colt.core.model.COLTProject;
import codeOrchestra.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;

/**
 * @author Alexander Eliseyev
 */
public abstract class COLTProjectPersistence<P extends COLTProject> {

    public abstract int getVersion();

    public void persist(P coltProject) throws COLTProjectPersistException {
        Document projectDocument = XMLUtils.createDocument();

        // Root
        Element rootElement = projectDocument.createElement("coltProject");
        rootElement.setAttribute("persistenceVersion", String.valueOf(getVersion()));
        rootElement.setAttribute("handlerId", coltProject.getHandlerId());
        rootElement.setAttribute("name", coltProject.getName());
        projectDocument.appendChild(rootElement);

        // Aspects
        Element aspectsElement = projectDocument.createElement("aspects");
        for (COLTProjectPersistedAspect coltProjectPersistedAspect : coltProject.getAllPersistedAspects()) {
            Element aspectElement = projectDocument.createElement(coltProjectPersistedAspect.getAspectName());
            aspectElement.setAttribute("name", coltProjectPersistedAspect.getAspectName());
            coltProjectPersistedAspect.persist(projectDocument, aspectElement, coltProject);
        }
        rootElement.appendChild(aspectsElement);

        // Handler-specific persistence
        Element handlerElement = projectDocument.createElement("handler");
        persistLanguageSpecific(projectDocument, handlerElement, coltProject);
        rootElement.appendChild(handlerElement);

        try {
            XMLUtils.saveToFile(coltProject.getPath(), projectDocument);
        } catch (TransformerException e) {
            throw new COLTProjectPersistException("Can't save the " + coltProject.getName() + " project under " + coltProject.getPath(), e);
        }
    }

    public P load(Element projectElement, String path) throws COLTProjectPersistException {
        P coltProject = createProject();

        // Root
        coltProject.setName(projectElement.getAttribute("name"));
        coltProject.setHandlerId(LiveCodingHandlerManager.getInstance().getCurrentHandler().getId());
        coltProject.setPath(path);

        // Aspects
        NodeList aspectsElements = projectElement.getElementsByTagName("aspects");
        if (aspectsElements != null) {
            Element aspectsElement = (Element) aspectsElements.item(0);
            if (aspectsElement != null) {
                NodeList aspectNodes = aspectsElement.getChildNodes();
                if (aspectNodes != null) {
                    for (int i = 0; i < aspectNodes.getLength(); i++) {
                        Node item = aspectNodes.item(i);
                        if (item instanceof Element) {
                            Element aspectElement = (Element) item;
                            String aspectName = aspectElement.getAttribute("name");

                            COLTProjectPersistedAspect persistedAspectByName = coltProject.getPersistedAspectByName(aspectName);
                            if (persistedAspectByName == null) {
                                throw new COLTProjectPersistException("Can't a COLT project from " + path + ": No aspect loader for " + aspectName);
                            }

                            persistedAspectByName.load(aspectElement, coltProject);
                        }
                    }
                }
            }
        }

        // Handler-specific loading
        NodeList handlersElement = projectElement.getElementsByTagName("handler");
        if (handlersElement == null || handlersElement.getLength() == 0) {
            throw new COLTProjectPersistException("Can't a COLT project from " + path + ": No metadata found");
        }
        Element handlerElement = (Element) handlersElement.item(0);
        loadLanguageSpecific(handlerElement, coltProject);

        return null;
    }

    protected abstract P createProject();

    protected abstract void persistLanguageSpecific(Document projectDocument, Element handlerElement, P coltProject);

    protected abstract void loadLanguageSpecific(Element handlerElement, P coltProject);

}
