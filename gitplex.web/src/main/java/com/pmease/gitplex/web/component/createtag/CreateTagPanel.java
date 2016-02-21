package com.pmease.gitplex.web.component.createtag;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class CreateTagPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final String revision;
	
	private String tagName;
	
	private String tagMessage;
	
	public CreateTagPanel(String id, IModel<Depot> depotModel, String revision) {
		super(id);
		this.depotModel = depotModel;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new NotificationPanel("feedback", form));
		
		final TextField<String> nameInput;
		form.add(nameInput = new TextField<String>("name", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tagName;
			}

			@Override
			public void setObject(String object) {
				tagName = object;
			}
			
		}));
		nameInput.setOutputMarkupId(true);
		
		form.add(new TextArea<String>("message", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tagMessage;
			}

			@Override
			public void setObject(String object) {
				tagMessage = object;
			}
			
		}));
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (tagName == null) {
					form.error("Tag name is required.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (!Repository.isValidRefName(Constants.R_HEADS + tagName)) {
					form.error("Tag name is not valid.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (depotModel.getObject().getObjectId(GitUtils.tag2ref(tagName), false) != null) {
					form.error("Tag '" + tagName + "' already exists, please choose a different name.");
					target.focusComponent(nameInput);
					target.add(form);
				} else {
					Depot repo = depotModel.getObject();
					User user = GitPlex.getInstance(UserManager.class).getCurrent();
					repo.tag(tagName, revision, user.asPerson(), tagMessage);
					onCreate(target, tagName);
				}
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}
	
	protected abstract void onCreate(AjaxRequestTarget target, String tag);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}

}