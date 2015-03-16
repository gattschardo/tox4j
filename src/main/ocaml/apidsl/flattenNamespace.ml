open ApiAst


let prepend_ns ns name =
  List.fold_left
    (fun (LName name) (LName ns) ->
       LName (ns ^ "_" ^ name)
    ) name ns


let transform decls =
  let open ApiFold in

  let fold_function_name v ns = function
    | Fn_Custom (type_name, lname) ->
        let _, type_name = v.fold_type_name v ns type_name in
        let lname = prepend_ns ns.ReplaceDecl.state lname in
        ns, Fn_Custom (type_name, lname)

    | function_name ->
        visit_function_name v ns function_name
  in

  let fold_decl v ns = function
    | Decl_Namespace (name, decls) ->
        let ns' = ReplaceDecl.({ ns with state = name :: ns.state }) in
        let _, decls = visit_list v.fold_decl v ns' decls in
        let ns = ReplaceDecl.replace ns decls in
        ns, Decl_Namespace (name, decls)

    | decl ->
        ReplaceDecl.fold_decl v ns decl
  in

  let v = {
    default with
    fold_function_name;
    fold_decl;
  } in
  snd @@ ReplaceDecl.fold_decls v (ReplaceDecl.initial []) decls