function DomainItem() {
  var gSobject = gs.inherit(gs.baseClass,'DomainItem');
  gSobject.clazz = { name: 'org.grooscript.domain.DomainItem', simpleName: 'DomainItem'};
  gSobject.clazz.superclass = { name: 'java.lang.Object', simpleName: 'Object'};
  gSobject.name = null;
  gSobject.date = null;
  gSobject.number = null;
  gSobject.__defineGetter__('listColumns', function(){ return DomainItem.listColumns; });
  gSobject.__defineSetter__('listColumns', function(gSval){ DomainItem.listColumns = gSval; });
  gSobject.__defineGetter__('listItems', function(){ return DomainItem.listItems; });
  gSobject.__defineSetter__('listItems', function(gSval){ DomainItem.listItems = gSval; });
  gSobject.__defineGetter__('lastId', function(){ return DomainItem.lastId; });
  gSobject.__defineSetter__('lastId', function(gSval){ DomainItem.lastId = gSval; });
  gSobject.id = null;
  gSobject.errors = gs.map();
  gSobject.version = 0;
  gSobject.__defineGetter__('changeListeners', function(){ return DomainItem.changeListeners; });
  gSobject.__defineSetter__('changeListeners', function(gSval){ DomainItem.changeListeners = gSval; });
  gSobject['clientValidations'] = function(it) {
    var result = true;
    var item = this;
    gSobject.errors = gs.map();
    gs.mc(DomainItem.listColumns,"each",gs.list([function(field) {
      if (gs.bool(gs.gp(field,"constraints"))) {
        if ((gs.equals((gs.gp(field,"constraints") [ "blank"]), false)) && (!gs.bool(gs.gp(item,"" + (gs.gp(field,"name")) + "")))) {
          gs.mc(gSobject.errors,"put",gs.list([gs.gp(field,"name"), gs.plus("blank validation on value ", gs.gp(item,"" + (gs.gp(field,"name")) + ""))]));
          return result = false;
        } else {
          return null;
        };
      } else {
        return null;
      };
    }]));
    return result;
  }
  gSobject['validate'] = function(it) {
    return gs.mc(gSobject,"clientValidations",gs.list([]));
  }
  gSobject['hasErrors'] = function(it) {
    return gSobject.errors;
  }
  gSobject.count = function() { return DomainItem.count(); }
  gSobject.list = function(x0) { return DomainItem.list(x0); }
  gSobject.get = function(x0) { return DomainItem.get(x0); }
  gSobject.obtainClonedItem = function(x0) { return DomainItem.obtainClonedItem(x0); }
  gSobject['save'] = function(it) {
    if (!gs.mc(gSobject,"clientValidations",gs.list([]))) {
      return false;
    } else {
      if (!gs.bool(gs.gp(gs.thisOrObject(this,gSobject),"id"))) {
        gs.sp(this,"id",++DomainItem.lastId);
        gs.mc(DomainItem.listItems,'leftShift', gs.list([this]));
        gs.mc(this,"processChanges",gs.list([gs.map().add("action","insert").add("item",this)]));
      } else {
        gs.mc(this,"processChanges",gs.list([gs.map().add("action","update").add("item",this)]));
      };
      return true;
    };
  }
  gSobject['delete'] = function(it) {
    if (gs.bool(gs.gp(gs.thisOrObject(this,gSobject),"id"))) {
      listItems = (gs.minus(listItems, this));
      gs.mc(this,"processChanges",gs.list([gs.map().add("action","delete").add("item",this)]));
      return gs.gp(gs.thisOrObject(this,gSobject),"id");
    } else {
      throw "Exception";
    };
  }
  gSobject.processChanges = function(x0) { return DomainItem.processChanges(x0); }
  if (arguments.length == 1) {gs.passMapToObject(arguments[0],gSobject);};
  
  return gSobject;
};
DomainItem.count = function(it) {
  return gs.mc(DomainItem.listItems,"size",gs.list([]));
}
DomainItem.list = function(params) {
  if (params === undefined) params = null;
  var result = gs.list([]);
  DomainItem.listItems.each(function(domainItem) {
    var clonedItem = gs.mc(this,"obtainClonedItem",gs.list([domainItem]));
    gs.mc(result,"add",gs.list([clonedItem]));
  });
  return result;
}
DomainItem.get = function(value) {
  var number = value;
  var item = gs.mc(DomainItem.listItems,"find",gs.list([function(it) {
    return gs.equals(gs.gp(it,"id"), gSobject.number);
  }]));
  return gs.mc(this,"obtainClonedItem",gs.list([item]));
}
DomainItem.obtainClonedItem = function(item) {
  if (gs.bool(item)) {
    var newItem = gs.mc(gs.classForName(gs.gp(item.clazz,"name")),"newInstance",gs.list([]));
    var copiedItem = item;
    gs.mc(DomainItem.listColumns,"each",gs.list([function(column) {
      return gs.sp(newItem,"" + (gs.gp(column,"name")) + "",gs.gp(copiedItem,"" + (gs.gp(column,"name")) + ""));
    }]));
    gs.sp(newItem,"id",gs.gp(copiedItem,"id"));
    return newItem;
  } else {
    return null;
  };
}
DomainItem.processChanges = function(data) {
  var actionData = data;
  if (gs.bool(DomainItem.changeListeners)) {
    return gs.mc(DomainItem.changeListeners,"each",gs.list([function(it) {
      return (it.delegate!=undefined?gs.applyDelegate(it,it.delegate,[actionData]):gs.executeCall(it, gs.list([actionData])));
    }]));
  } else {
    return null;
  };
}
DomainItem.listColumns = gs.list([gs.expando(gs.map().add("name","name").add("type","java.lang.String").add("constraints",gs.map().add("nullable",false).add("maxSize",12))) , gs.expando(gs.map().add("name","date").add("type","java.util.Date").add("constraints",gs.map().add("nullable",true))) , gs.expando(gs.map().add("name","number").add("type","java.lang.Integer").add("constraints",gs.map().add("nullable",true)))]);
DomainItem.listItems = gs.list([]);
DomainItem.lastId = 0;
DomainItem.changeListeners = gs.list([]);

